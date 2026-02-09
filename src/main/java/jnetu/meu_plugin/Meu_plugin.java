package jnetu.meu_plugin;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import jnetu.meu_plugin.economy.MoedaEconomy;
import jnetu.meu_plugin.skill.*;
import jnetu.meu_plugin.util.PluginsListCustomizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.UUID;

public final class Meu_plugin extends JavaPlugin implements Listener {

    private final HashSet<UUID> modoGeloAtivo = new HashSet<>();
    private final String TITULO_GUI = "Menu de Habilidades";

    @Override
    public void onEnable() {
        getLogger().info("=========================================");
        getLogger().info(" Iniciando Plugin de Skills (Jnetu)");
        getLogger().info("=========================================");

        // 1. Registra economia no Vault
        carregarEconomia();

        // 2. Aguarda o AuraSkills carregar completamente
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (getServer().getPluginManager().isPluginEnabled("AuraSkills")) {
                carregarAuraSkills();
            } else {
                getLogger().warning("AuraSkills não encontrado! Skills customizadas desabilitadas.");
            }

            carregarBukkit();
            getLogger().info("✅ Plugin carregado com sucesso!");
        }, 20L); // 1 segundo
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
            }, 20L); // 1 segundo depois

            getLogger().info("✅ Integrações com AuraSkills carregadas!");

        } catch (Exception e) {
            getLogger().severe("❌ Erro ao carregar AuraSkills:");
            e.printStackTrace();
        }
    }

    // ==================== BUKKIT ====================

    private void carregarBukkit() {
        // Registra eventos
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new PluginsListCustomizer(this), this);

        // Registra comandos
        registrarComando("sethome");
        registrarComando("home");
        registrarComando("habilidades");
        registrarComando("balance");
        registrarComando("darmoeda");

        getLogger().info("✅ Comandos e eventos registrados!");
    }

    private void registrarComando(String nomeComando) {
        var cmd = getCommand(nomeComando);
        if (cmd != null) {
            cmd.setExecutor(this);
        } else {
            getLogger().warning("⚠ Comando '/" + nomeComando + "' não está no plugin.yml!");
        }
    }

    // ==================== EVENTOS ====================

    @EventHandler
    public void aoEntrar(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Recarrega estado da habilidade
        if (getConfig().getBoolean("habilidades." + uuid + ".passo-gelado")) {
            modoGeloAtivo.add(uuid);
        }

        // Mensagens de boas-vindas
        player.sendMessage(Component.text("Bem-vindo ao servidor.", NamedTextColor.GREEN));

        Component mensagem = Component.text("O jogador ", NamedTextColor.GRAY)
                .append(player.name().color(NamedTextColor.YELLOW))
                .append(Component.text(" entrou no servidor!", NamedTextColor.GRAY));
        Bukkit.broadcast(mensagem);
    }

    @EventHandler
    public void aoClicarNoMenu(InventoryClickEvent event) {
        if (!event.getView().title().equals(Component.text(TITULO_GUI))) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();

        // Alterna habilidade
        if (modoGeloAtivo.contains(uuid)) {
            modoGeloAtivo.remove(uuid);
            salvarEstadoGelo(uuid, false);
            player.sendMessage(Component.text("Passo Gelado ", NamedTextColor.AQUA)
                    .append(Component.text("DESATIVADO", NamedTextColor.RED)));
        } else {
            modoGeloAtivo.add(uuid);
            salvarEstadoGelo(uuid, true);
            player.sendMessage(Component.text("Passo Gelado ", NamedTextColor.AQUA)
                    .append(Component.text("ATIVADO", NamedTextColor.GREEN)));
        }

        player.closeInventory();
    }

    @EventHandler
    public void aoAndar(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!modoGeloAtivo.contains(player.getUniqueId())) return;

        Location loc = event.getTo();
        if (loc == null) return;

        Block blockAbaixo = loc.clone().add(0, -1, 0).getBlock();

        if (blockAbaixo.getType() == Material.WATER) {
            if (blockAbaixo.getBlockData() instanceof org.bukkit.block.data.Levelled levelled) {
                if (levelled.getLevel() == 0 && blockAbaixo.getRelative(0, 1, 0).getType() == Material.AIR) {
                    blockAbaixo.setType(Material.ICE);
                }
            }
        }
    }

    @EventHandler
    public void aoAtacar(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) return;
        if (item.getItemMeta().getCustomModelData() != 1001) return;

        if (event.getEntity() instanceof org.bukkit.entity.LivingEntity vitima) {
            vitima.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SLOWNESS, 100, 1));
            player.sendMessage(Component.text("Inimigo congelado!", NamedTextColor.AQUA));
        }
    }

    // ==================== COMANDOS ====================

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores podem usar este comando.");
            return true;
        }

        String cmd = command.getName().toLowerCase();

        return switch (cmd) {
            case "sethome" -> comandoSetHome(player);
            case "home" -> comandoHome(player);
            case "habilidades" -> comandoHabilidades(player);
            case "balance" -> comandoBalance(player);
            case "darmoeda" -> comandoDarMoeda(player, args);
            default -> false;
        };
    }

    private boolean comandoSetHome(Player player) {
        Location loc = player.getLocation();
        String uuid = player.getUniqueId().toString();

        getConfig().set("homes." + uuid + ".world", loc.getWorld().getName());
        getConfig().set("homes." + uuid + ".x", loc.getX());
        getConfig().set("homes." + uuid + ".y", loc.getY());
        getConfig().set("homes." + uuid + ".z", loc.getZ());
        getConfig().set("homes." + uuid + ".yaw", loc.getYaw());
        getConfig().set("homes." + uuid + ".pitch", loc.getPitch());
        saveConfig();

        player.sendMessage(Component.text("✓ Home definida!", NamedTextColor.GREEN));
        return true;
    }

    private boolean comandoHome(Player player) {
        String uuid = player.getUniqueId().toString();

        if (!getConfig().contains("homes." + uuid)) {
            player.sendMessage(Component.text("Você não tem home! Use /sethome.", NamedTextColor.RED));
            return true;
        }

        String world = getConfig().getString("homes." + uuid + ".world");
        double x = getConfig().getDouble("homes." + uuid + ".x");
        double y = getConfig().getDouble("homes." + uuid + ".y");
        double z = getConfig().getDouble("homes." + uuid + ".z");
        float yaw = (float) getConfig().getDouble("homes." + uuid + ".yaw");
        float pitch = (float) getConfig().getDouble("homes." + uuid + ".pitch");

        Location home = new Location(getServer().getWorld(world), x, y, z, yaw, pitch);
        player.teleport(home);
        player.sendMessage(Component.text("✓ Teleportado para sua home!", NamedTextColor.AQUA));
        return true;
    }

    private boolean comandoHabilidades(Player player) {
        abrirMenuHabilidades(player);
        return true;
    }

    private boolean comandoBalance(Player player) {
        Economy economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();

        if (economy == null) {
            player.sendMessage(Component.text("Economia indisponível!", NamedTextColor.RED));
            return true;
        }

        double saldo = economy.getBalance(player);

        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GRAY));
        player.sendMessage(Component.text("💰 Seu Saldo", NamedTextColor.GOLD, TextDecoration.BOLD));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("  Moedas: ", NamedTextColor.YELLOW)
                .append(Component.text(String.format("%.2f", saldo), NamedTextColor.WHITE)));
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GRAY));
        return true;
    }

    private boolean comandoDarMoeda(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(Component.text("Use: /darmoeda <quantia>", NamedTextColor.RED));
            return true;
        }

        try {
            double quantia = Double.parseDouble(args[0]);
            Economy economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();

            if (economy == null) {
                player.sendMessage(Component.text("Economia indisponível!", NamedTextColor.RED));
                return true;
            }

            economy.depositPlayer(player, quantia);
            player.sendMessage(Component.text("✓ Você recebeu ", NamedTextColor.GREEN)
                    .append(Component.text(String.format("%.2f Moedas!", quantia), NamedTextColor.GOLD)));

        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Valor inválido!", NamedTextColor.RED));
        }
        return true;
    }

    // ==================== UTILIDADES ====================

    private void abrirMenuHabilidades(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text(TITULO_GUI));
        ItemStack itemGelo = new ItemStack(Material.ICE);
        ItemMeta meta = itemGelo.getItemMeta();

        boolean ativo = modoGeloAtivo.contains(player.getUniqueId());

        meta.displayName(Component.text("Passo Gelado", NamedTextColor.AQUA));
        meta.lore(java.util.List.of(
                Component.text("Status: " + (ativo ? "ATIVADO" : "DESATIVADO"),
                        ativo ? NamedTextColor.GREEN : NamedTextColor.RED)
        ));

        itemGelo.setItemMeta(meta);
        gui.setItem(13, itemGelo);
        player.openInventory(gui);
    }

    private void salvarEstadoGelo(UUID uuid, boolean ativo) {
        getConfig().set("habilidades." + uuid + ".passo-gelado", ativo);
        saveConfig();
    }
}
