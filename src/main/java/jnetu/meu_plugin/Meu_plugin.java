package jnetu.meu_plugin;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import jnetu.meu_plugin.comandos.ComandosManager;
import jnetu.meu_plugin.economy.MoedaEconomy;
import jnetu.meu_plugin.habilidades.HabilidadesManager;
import jnetu.meu_plugin.itens.ItensManager;
import jnetu.meu_plugin.menu.MenuListener;
import jnetu.meu_plugin.skill.*;
import jnetu.meu_plugin.util.PluginsListCustomizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class Meu_plugin extends JavaPlugin implements Listener {

    // Gerenciadores
    private HabilidadesManager habilidadesManager;
    private ItensManager itensManager;
    private ComandosManager comandosManager;

    @Override
    public void onEnable() {
        getLogger().info("=========================================");
        getLogger().info(" Iniciando Plugin de Skills (Jnetu)");
        getLogger().info("=========================================");

        // 1. Inicializa gerenciadores
        this.habilidadesManager = new HabilidadesManager(this);
        this.itensManager = new ItensManager(this);
        this.comandosManager = new ComandosManager(this);

        // 2. Registra economia no Vault
        carregarEconomia();

        // 3. Aguarda o AuraSkills carregar completamente
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (getServer().getPluginManager().isPluginEnabled("AuraSkills")) {
                carregarAuraSkills();
            } else {
                getLogger().warning("AuraSkills não encontrado! Skills customizadas desabilitadas.");
            }

            carregarBukkit();
            getLogger().info("✅ Plugin carregado com sucesso!");
        }, 20L);
    }

    @Override
    public void onDisable() {
        // Remove as injeções ao desabilitar o plugin
        if (getServer().getPluginManager().isPluginEnabled("AuraSkills")) {
            AuraSkillsConfigManager configManager = new AuraSkillsConfigManager(this);
            configManager.removerInjecoes();
        }
        getLogger().info("Plugin desabilitado.");
    }

    // ==================== ECONOMIA ====================

    private void carregarEconomia() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("⚠ Vault não encontrado! Economia desabilitada.");
            return;
        }

        MoedaEconomy minhaEconomia = new MoedaEconomy(this);
        getServer().getServicesManager().register(
                Economy.class,
                minhaEconomia,
                this,
                ServicePriority.Highest
        );

        getLogger().info("✅ Economia registrada no Vault!");
    }

    // ==================== AURASKILLS ====================

    private void carregarAuraSkills() {
        try {
            AuraSkillsApi api = AuraSkillsApi.get();
            NamespacedRegistry registry = api.useRegistry("meu_plugin", getDataFolder());

            // Registra Stat e Trait customizados
            registry.registerStat(MinhasStats.CARISMA);
            registry.registerTrait(MinhasTraits.REDUCAO_BATERIA);

            // Registra handler do trait
            CarismaHandler carismaHandler = new CarismaHandler(api);
            api.getHandlers().registerTraitHandler(carismaHandler);

            // Registra Skill customizada
            registry.registerSkill(MinhasSkills.SOCIAL);

            // Registra Source Type customizado
            registry.registerSourceType("chat_battery", (sourceNode, context) -> {
                long recharge = sourceNode.node("recharge_seconds").getLong(600);
                return new SocialSource(context.parseValues(sourceNode), recharge);
            });

            // Registra listener da skill Social
            getServer().getPluginManager().registerEvents(
                    new SocialLeveler(this, api),
                    this
            );

            // ✅ INJETA CONFIGURAÇÕES NOS MENUS DO AURASKILLS
            AuraSkillsConfigManager configManager = new AuraSkillsConfigManager(this);
            configManager.injetarConfiguracoes();

            // ✅ FORÇA O AURASKILLS A RECARREGAR OS MENUS
            Bukkit.getScheduler().runTaskLater(this, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "skills reload");
                getLogger().info("✅ AuraSkills recarregado com novas configurações!");
            }, 20L);

            getLogger().info("✅ Integrações com AuraSkills carregadas!");

        } catch (Exception e) {
            getLogger().severe("❌ Erro ao carregar AuraSkills:");
            e.printStackTrace();
        }
    }

    // ==================== BUKKIT ====================

    private void carregarBukkit() {
        // Registra eventos principais
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new PluginsListCustomizer(this), this);

        // Registra listeners dos gerenciadores
        getServer().getPluginManager().registerEvents(new MenuListener(this, habilidadesManager), this);
        habilidadesManager.registrarListeners();
        itensManager.registrarListeners();

        getLogger().info("✅ Comandos e eventos registrados!");
    }

    // ==================== EVENTOS ====================

    @EventHandler
    public void aoEntrar(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Carrega estado das habilidades
        habilidadesManager.carregarEstadoJogador(player);

        // Mensagens de boas-vindas
        player.sendMessage(Component.text("Bem-vindo ao servidor.", NamedTextColor.GREEN));

        Component mensagem = Component.text("O jogador ", NamedTextColor.GRAY)
                .append(player.name().color(NamedTextColor.YELLOW))
                .append(Component.text(" entrou no servidor!", NamedTextColor.GRAY));
        Bukkit.broadcast(mensagem);
    }

    // ==================== GETTERS ====================

    public HabilidadesManager getHabilidadesManager() {
        return habilidadesManager;
    }

    public ItensManager getItensManager() {
        return itensManager;
    }

    public ComandosManager getComandosManager() {
        return comandosManager;
    }
}
