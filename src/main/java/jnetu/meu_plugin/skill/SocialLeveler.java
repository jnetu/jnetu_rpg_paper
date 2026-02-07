package jnetu.meu_plugin.skill;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.user.SkillsUser;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Leveler da Skill Social com sistema de "bateria social"
 * 
 * Como funciona:
 * - Cada jogador tem uma bateria que recarrega com o tempo
 * - O stat Carisma reduz o tempo de recarga (10% por ponto)
 * - Ao falar no chat, consome a bateria e ganha XP proporcional
 * - XP baixo: silencioso (sem ActionBar)
 * - XP alto: com feedback (ActionBar + som)
 */
public class SocialLeveler implements Listener {

    private final JavaPlugin plugin;
    private final AuraSkillsApi api;
    private final Map<UUID, SocialData> socialBattery = new ConcurrentHashMap<>();

    public SocialLeveler(JavaPlugin plugin, AuraSkillsApi api) {
        this.plugin = plugin;
        this.api = api;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void aoFalarNoChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Busca a configuração da skill Social
        SkillSource<SocialSource> skillSourceWrapper = api.getSourceManager()
                .getSingleSourceOfType(SocialSource.class);
        
        if (skillSourceWrapper == null) {
            plugin.getLogger().warning("SocialSource não encontrado! Verifique o arquivo social.yml");
            return;
        }

        SocialSource sourceConfig = skillSourceWrapper.source();
        Skill socialSkill = skillSourceWrapper.skill();

        // Calcula XP baseado na bateria (thread-safe)
        double xpGanho = calcularXpGanho(player, uuid, sourceConfig);
        
        // Ignora ganhos muito pequenos
        if (xpGanho < 0.1) return;

        // Move para thread principal (obrigatório para modificar XP)
        Bukkit.getScheduler().runTask(plugin, () -> 
            processarGanhoXp(player, socialSkill, xpGanho, sourceConfig)
        );
    }

    /**
     * Calcula quanto XP o jogador deve ganhar baseado na bateria social
     * Thread-safe: pode ser chamado da thread assíncrona do chat
     */
    private double calcularXpGanho(Player player, UUID uuid, SocialSource sourceConfig) {
        SocialData data = atualizarBateria(player, uuid, sourceConfig.getRechargeMs());

        data.lock.lock();
        try {
            double carga = data.cargaAtual;
            data.cargaAtual = 0.0; // Esvazia a bateria ao usar
            data.ultimoUpdate = System.currentTimeMillis();

            return carga * sourceConfig.getXp();
        } finally {
            data.lock.unlock();
        }
    }

    /**
     * Processa o ganho de XP na thread principal
     * XP baixo: silencioso (sem ActionBar, sem som)
     * XP alto: com feedback completo (ActionBar + som extra)
     */
    private void processarGanhoXp(Player player, Skill skill, double xpGanho, SocialSource config) {
        if (!player.isOnline()) return;

        SkillsUser user = api.getUser(player.getUniqueId());
        if (user == null) return;

        double limiteParaSom = config.getXp() / 2;

        if (xpGanho >= limiteParaSom) {
            // GANHO ALTO: Mostra ActionBar + Som padrão do AuraSkills + Som extra
            user.addSkillXp(skill, xpGanho);
            player.playSound(player.getLocation(), 
                Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2.0f);
        } else {
            // GANHO BAIXO: Adiciona XP silenciosamente
            // O setSkillXp() da API já chama checkLevelUp() internamente
            double xpAtual = user.getSkillXp(skill);
            user.setSkillXp(skill, xpAtual + xpGanho);
        }
    }

    /**
     * Atualiza a bateria social baseada no tempo passado
     * Aplica o bônus de Carisma na velocidade de recarga
     * Recarga de 0% a 100% baseado no tempo configurado no YML
     */
    private SocialData atualizarBateria(Player player, UUID uuid, long tempoRecargaBase) {
        SocialData data = socialBattery.computeIfAbsent(uuid, 
            k -> new SocialData(System.currentTimeMillis()));

        data.lock.lock();
        try {
            long agora = System.currentTimeMillis();
            long tempoPassado = agora - data.ultimoUpdate;

            // Aplica o bônus de Carisma
            long tempoRecargaComBonus = calcularTempoRecargaComCarisma(player, tempoRecargaBase);

            // Calcula quanto % recarregou baseado no tempo
            // Exemplo: se passou metade do tempo de recarga, recarrega 50%
            double recarga = (double) tempoPassado / tempoRecargaComBonus;

            data.cargaAtual = Math.min(1.0, data.cargaAtual + recarga);
            data.ultimoUpdate = agora;

            return data;
        } finally {
            data.lock.unlock();
        }
    }

    /**
     * Calcula o tempo de recarga com o bônus de Carisma
     * Cada ponto de Carisma reduz 10% do tempo de recarga
     * 
     * Exemplos:
     * - 0 Carisma: 600s (100%)
     * - 1 Carisma: 540s (90%)
     * - 2 Carisma: 480s (80%)
     * - 5 Carisma: 300s (50%)
     * - 10 Carisma: 0s (recarga instantânea)
     */
    private long calcularTempoRecargaComCarisma(Player player, long tempoBase) {
        SkillsUser user = api.getUser(player.getUniqueId());
        if (user == null) return tempoBase;

        // ERRADO (Hardcoded):
        // double carisma = user.getStatLevel(MinhasStats.CARISMA);
        // double reducao = carisma * 0.10;
        double valorTrait = user.getEffectiveTraitLevel(MinhasTraits.REDUCAO_BATERIA);

        // %
        double porcentagemReducao = valorTrait / 100.0;

        // Trava em 100% - evita tempo negativo ou bugs
        if (porcentagemReducao > 1.0) porcentagemReducao = 1.0;

        // Calcula o tempo final
        return (long) (tempoBase * (1.0 - porcentagemReducao));
    }

    @EventHandler
    public void aoEntrar(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        // putIfAbsent evita sobrescrever dados se já existirem
        socialBattery.putIfAbsent(uuid, new SocialData(System.currentTimeMillis()));
    }

    @EventHandler
    public void aoSair(PlayerQuitEvent event) {
        // Remove dados do jogador ao sair para liberar memória
        socialBattery.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Dados da bateria social do jogador
     * Thread-safe usando ReentrantLock
     */
    private static class SocialData {
        private final ReentrantLock lock = new ReentrantLock();
        private double cargaAtual = 0.0; // 0.0 a 1.0 (0% a 100%)
        private long ultimoUpdate;

        public SocialData(long ultimoUpdate) {
            this.ultimoUpdate = ultimoUpdate;
        }
    }
}
