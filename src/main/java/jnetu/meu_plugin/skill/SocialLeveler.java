package jnetu.meu_plugin.skill;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.aurelium.auraskills.bukkit.AuraSkills;
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

public class SocialLeveler implements Listener {

    private final JavaPlugin plugin;
    private final AuraSkillsApi api;
    private final AuraSkills auraSkills;
    private final Map<UUID, SocialData> socialBattery = new ConcurrentHashMap<>();

    public SocialLeveler(JavaPlugin plugin, AuraSkillsApi api) {
        this.plugin = plugin;
        this.api = api;
        this.auraSkills = (AuraSkills) Bukkit.getPluginManager().getPlugin("AuraSkills");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoFalarNoChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        SkillSource<SocialSource> skillSourceWrapper = api.getSourceManager().getSingleSourceOfType(SocialSource.class);
        if (skillSourceWrapper == null) return;
        SocialSource sourceConfig = skillSourceWrapper.source();
        double xpGanho = calcularXpGanho(uuid, sourceConfig); //fazer na thread chat

//        SocialData data = atualizarBateria(uuid, sourceConfig.getRechargeMs());
//        double carga;
//        synchronized (data) {
//            carga = data.cargaAtual;
//            data.cargaAtual = 0.0;
//            data.ultimoUpdate = System.currentTimeMillis();
//        }
//
//        double xpGanho = carga * sourceConfig.getXp();//variavel dentro de um yml//;
//
        if (xpGanho < 0.1) return;
        Skill cashSkill =  skillSourceWrapper.skill();
        Bukkit.getScheduler().runTask(plugin, () -> {
            //SE NADA FUNCIONAR NESSA PORRA USE ISSO:
//            if (player.isOnline()) {
//                //api.getUser(uuid).addSkillXp(cashSkill, xpGanho);
//
//                SkillsUser user = api.getUser(event.getPlayer().getUniqueId());
//
//                double limiteSilencioso = sourceConfig.getXp() / 2;
//
//
//                if (xpGanho >= limiteSilencioso) {
//                    // GANHO ALTO: Mostra Action Bar + Toca Som Padrão
//                    user.addSkillXp(cashSkill, xpGanho);
//                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2.0f);
//
//                } else {
//                    //gambiarra para não mostrar barra de xp
//                    double xpAtual = user.getSkillXp(cashSkill);
//                    user.setSkillXp(cashSkill, xpAtual + xpGanho);
//                }
//
//            }
            processarGanhoXp(player, cashSkill, xpGanho, sourceConfig);
        });
    }


    private void processarGanhoXp(Player player, Skill skill, double xpGanho, SocialSource config) {
        if (!player.isOnline()) return;

        SkillsUser user = api.getUser(player.getUniqueId());
        if (user == null) return;

        double limiteSilencioso = config.getXp() / 2;

        if (xpGanho >= limiteSilencioso) {
            // GANHO ALTO: Mostra Action Bar + Toca Som Padrão
            user.addSkillXp(skill, xpGanho);
            player.playSound(player.getLocation(),
                    Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2.0f);
        } else {
            //gambiarra para não mostrar barra de xp
            double xpAtual = user.getSkillXp(skill);
            user.setSkillXp(skill, xpAtual + xpGanho);
        }
    }


    /**
     * Calcula quanto XP o jogador deve ganhar baseado na bateria social
     * Thread-safe: pode ser chamado da thread assíncrona do chat
     */
    private double calcularXpGanho(UUID uuid, SocialSource sourceConfig) {
        SocialData data = atualizarBateria(uuid, sourceConfig.getRechargeMs());

        data.lock.lock();
        try {
            double carga = data.cargaAtual;
            data.cargaAtual = 0.0; // Esvazia a bateria
            data.ultimoUpdate = System.currentTimeMillis();

            return carga * sourceConfig.getXp();
        } finally {
            data.lock.unlock();
        }
    }



    /**
     * Atualiza a bateria social baseada no tempo passado
     * Recarga de 0% a 100% baseado no tempo configurado no YML
     */
    private SocialData atualizarBateria(UUID uuid, long tempoRecargaMs) {
        SocialData data = socialBattery.computeIfAbsent(uuid,
                k -> new SocialData(System.currentTimeMillis()));

        data.lock.lock();
        try {
            long agora = System.currentTimeMillis();
            long tempoPassado = agora - data.ultimoUpdate;

            // Cálculo baseado no tempo configurado no YML
            //Exe: se passou metade do tempo, recarrega 50%
            double recarga = (double) tempoPassado / tempoRecargaMs;

            data.cargaAtual = Math.min(1.0, data.cargaAtual + recarga);
            data.ultimoUpdate = agora;

            return data;
        } finally {
            data.lock.unlock();
        }
    }

    @EventHandler
    public void aoEntrar(PlayerJoinEvent event) {
        // putIfAbsent evita sobrescrever se já existir
        socialBattery.putIfAbsent(event.getPlayer().getUniqueId(), new SocialData(System.currentTimeMillis()));
    }
    @EventHandler
    public void aoSair(PlayerQuitEvent event) {

        socialBattery.remove(event.getPlayer().getUniqueId());
    }

//    private static class SocialData {
//        double cargaAtual;
//        long ultimoUpdate;
//
//        public SocialData(double cargaAtual, long ultimoUpdate) {
//            this.cargaAtual = cargaAtual;
//            this.ultimoUpdate = ultimoUpdate;
//        }
//    }
private static class SocialData {
    private final ReentrantLock lock = new ReentrantLock();
    private double cargaAtual = 0.0; // 0.0 a 1.0 (0% a 100%)
    private long ultimoUpdate;

    public SocialData(long ultimoUpdate) {
        this.ultimoUpdate = ultimoUpdate;
    }
}
}