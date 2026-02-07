package jnetu.meu_plugin;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import jnetu.meu_plugin.skill.*;
import jnetu.meu_plugin.util.PluginsListCustomizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

public final class Meu_plugin extends JavaPlugin implements Listener {

    private final HashSet<UUID> modoGeloAtivo = new HashSet<>();
    private final String TITULO_GUI = "Menu de Habilidades";

    @Override
    public void onEnable() {

        getLogger().info("=========================================");
        getLogger().info(" Iniciando Plugin de Skills (Jnetu)");
        getLogger().info("=========================================");

        salvarArquivo("stats.yml");
        salvarArquivo("sources/social.yml");
        salvarArquivo("rewards/social.yml");

        // CONFIGURAÇÃO AURASKILLS
        carregarAuraSkills();

        // CONFIGURAÇÃO BUKKIT Comandos Eventos
        carregarBukkit();

        getLogger().info("Plugin totalmente carregado e pronto!");
    }


    /**
     * Gerencia toda a lógica de carregamento da API AuraSkills
     */
    private void carregarAuraSkills() {
        AuraSkillsApi auraSkills = AuraSkillsApi.get();
        NamespacedRegistry registry = auraSkills.useRegistry("meu_plugin", getDataFolder());

        registry.registerTrait(MinhasTraits.REDUCAO_BATERIA);
        registry.registerStat(MinhasStats.CARISMA);
        getLogger().info("[AuraSkills] Stats e Traits registrados.");

        CarismaHandler carismaHandler = new CarismaHandler(auraSkills);
        auraSkills.getHandlers().registerTraitHandler(carismaHandler);

        registry.registerSkill(MinhasSkills.SOCIAL);
        getLogger().info("[AuraSkills] Skills registradas.");



        registry.registerSourceType("chat_battery", (sourceNode, context) -> {
            long recharge = sourceNode.node("recharge_seconds").getLong(600);
            return new SocialSource(context.parseValues(sourceNode), recharge);
        });
        getLogger().info("[AuraSkills] SourceType 'chat_battery' registrado.");

        getServer().getPluginManager().registerEvents(
                new SocialLeveler(this, auraSkills),
                this
        );


        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                AuraSkillsMenuInjector injector = new AuraSkillsMenuInjector(this);
                injector.injetarContextosCustomizados();  // Injeta stat Carisma
                injector.injetarContextoTrait();          // Injeta trait chat_battery
                injector.injetarContextoSkill();          // Injeta skill Social

                getLogger().info("✓ Menus do AuraSkills modificados com sucesso!");
                getLogger().info("  Execute /skills reload ou reinicie o servidor");
            } catch (Exception e) {
                getLogger().warning("Não foi possível injetar menus no AuraSkills: " + e.getMessage());
                getLogger().warning("Isso é normal se for a primeira vez rodando o plugin.");
            }
        }, 20L); // Espera 1 segundo (20 ticks)


    }

    /**
     * Gerencia comandos e eventos padrões do Minecraft
     */
    private void carregarBukkit() {
        // Registra eventos da classe principal (se houver @EventHandler nesta classe)
        getServer().getPluginManager().registerEvents(this, this);

        // Registra comandos de forma segura
        registrarComando("sethome");
        registrarComando("home");
        registrarComando("habilidades");

        // Customizar comando /plugins
        getServer().getPluginManager().registerEvents(
                new PluginsListCustomizer(this),
                this
        );

        getLogger().info("Comando /plugins customizado!");
    }

    /**
     * Salva um arquivo da pasta resources apenas se ele não existir.
     */
    private void salvarArquivo(String caminho) {
        File arquivo = new File(getDataFolder(), caminho);
        if (!arquivo.exists()) {
            saveResource(caminho, false);
            getLogger().info("Arquivo gerado: " + caminho);
        }
    }

    /**
     * Registra um comando
     */
    private void registrarComando(String nomeComando) {
        var pluginCommand = getCommand(nomeComando);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
        } else {
            getLogger().warning("ERRO: O comando '/" + nomeComando + "' não foi definido no plugin.yml!");
        }
    }

    public void onDisable() {
        try {
            AuraSkillsMenuInjector injector = new AuraSkillsMenuInjector(this);
            injector.removerInjections();
            getLogger().info("Configurações do AuraSkills restauradas");
        } catch (Exception e) {
            // Ignora erros ao desinstalar
        }
    }

    @EventHandler
    public void aoEntrar(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        //HABILIDADES
        UUID uuid = player.getUniqueId();
        if (getConfig().getBoolean("habilidades." + uuid + ".passo-gelado")) {
            modoGeloAtivo.add(uuid); // Reativa a habilidade na variável
        }

        //mensagem só para o player
        player.sendMessage(Component.text("Bem-vindo ao servidor.", NamedTextColor.GREEN));
        //mensagem global
        Component mensagemGlobal = Component.text("O jogador ", NamedTextColor.GRAY)
                .append(player.name().color(NamedTextColor.YELLOW))
                .append(Component.text(" acabou de entrar!", NamedTextColor.GRAY));

        Bukkit.broadcast(mensagemGlobal);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Verificar se quem digitou foi um jogador (console não tem "home")
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Apenas jogadores podem usar comandos");
            return true;
        }

        if (command.getName().equalsIgnoreCase("testaritem")) {
            ItemStack espada = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta meta = espada.getItemMeta();

            if (meta != null) {
                meta.displayName(Component.text("Espada de Gelo", NamedTextColor.AQUA));
                meta.setCustomModelData(1001); // O Bukkit cuida da sintaxe da 1.21 sozinho
                espada.setItemMeta(meta);
                player.getInventory().addItem(espada);
                player.sendMessage("§bItem de teste entregue!");
            }
            return true;
        }

        // COMANDO /SETHOME
        if (command.getName().equalsIgnoreCase("sethome")) {
            Location loc = player.getLocation();
            String uuid = player.getUniqueId().toString();

            // Salva config.yml
            getConfig().set("homes." + uuid + ".world", loc.getWorld().getName());
            getConfig().set("homes." + uuid + ".x", loc.getX());
            getConfig().set("homes." + uuid + ".y", loc.getY());
            getConfig().set("homes." + uuid + ".z", loc.getZ());
            getConfig().set("homes." + uuid + ".yaw", loc.getYaw());
            getConfig().set("homes." + uuid + ".pitch", loc.getPitch());

            saveConfig(); // end Salva

            player.sendMessage(Component.text("Home definida com sucesso!", NamedTextColor.GREEN));
            return true;
        }

        // COMANDO /HOME
        if (command.getName().equalsIgnoreCase("home")) {
            String uuid = player.getUniqueId().toString();

            if (!getConfig().contains("homes." + uuid)) {
                player.sendMessage(Component.text("Você não tem uma home definida! Use /sethome.", NamedTextColor.RED));
                return true;
            }

            // Recupera os dados do arquivo
            String worldName = getConfig().getString("homes." + uuid + ".world");
            double x = getConfig().getDouble("homes." + uuid + ".x");
            double y = getConfig().getDouble("homes." + uuid + ".y");
            double z = getConfig().getDouble("homes." + uuid + ".z");
            float yaw = (float) getConfig().getDouble("homes." + uuid + ".yaw");
            float pitch = (float) getConfig().getDouble("homes." + uuid + ".pitch");

            Location homeLoc = new Location(getServer().getWorld(worldName), x, y, z, yaw, pitch);

            player.teleport(homeLoc);
            player.sendMessage(Component.text("Teleportado para sua home!", NamedTextColor.AQUA));
            return true;
        }
        //end /HOME

        //HABILIDADES GUI
        if (command.getName().equalsIgnoreCase("habilidades")) {
                abrirMenuHabilidades(player);
            return true;
        }


        return false;
    }

    private void abrirMenuHabilidades(Player player) {

        Inventory gui = Bukkit.createInventory(null, 27, Component.text(TITULO_GUI));

        // Cria o item que representa a habilidade
        ItemStack itemGelo = new ItemStack(Material.ICE);
        ItemMeta meta = itemGelo.getItemMeta();

        meta.displayName(Component.text("Habilidade: Passo Gelado", NamedTextColor.AQUA));

        // Altera a descrição com base no estado atual
        boolean estaAtivo = modoGeloAtivo.contains(player.getUniqueId());
        meta.lore(java.util.List.of(
                Component.text("Status: " +
                                (estaAtivo ? "ATIVADO" : "DESATIVADO"),
                        estaAtivo ? NamedTextColor.GREEN : NamedTextColor.RED)
        ));

        itemGelo.setItemMeta(meta);
        gui.setItem(13, itemGelo);

        player.openInventory(gui);
    }

    @EventHandler
    public void aoClicarNoMenu(InventoryClickEvent event) {
        // Verificar se é menu
        if (!event.getView().title().equals(Component.text(TITULO_GUI))) return;

        event.setCancelled(true); // Impede o player de pegar o item do menu

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();

        // Alterna o estado da habilidade
        if (modoGeloAtivo.contains(uuid)) {
            modoGeloAtivo.remove(uuid);
            salvarEstadoGelo(uuid, false);

            player.sendMessage(Component.text("Habilidade de Gelo", NamedTextColor.WHITE)
                    .append(Component.text(" DESATIVADA!", NamedTextColor.RED)));
        } else {
            modoGeloAtivo.add(uuid);
            salvarEstadoGelo(uuid, true);
            player.sendMessage(Component.text("Habilidade de Gelo", NamedTextColor.WHITE)
                    .append(Component.text(" ATIVADA!", NamedTextColor.GREEN)));

            ;
        }

        player.closeInventory(); // Fecha o menu
    }
    private boolean modoGeloPlayer(UUID uuid){
        return modoGeloAtivo.contains(uuid);
    }

    private void salvarEstadoGelo(UUID uuid, boolean ativo) {
        getConfig().set("habilidades." + uuid + ".passo-gelado", ativo);
        saveConfig();
    }

    @EventHandler
    public void movement(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!modoGeloAtivo.contains(player.getUniqueId())) return;

        // --- CONFIGURAÇÃO ---
        int raio = 0;
        Location loc = event.getTo();
        if (loc == null) return;
        for (int x = -raio; x <= raio; x++) {
            for (int z = -raio; z <= raio; z++) {
                Block block = loc.clone().add(x, -1, z).getBlock();
                if (block.getType() == Material.WATER) {
                    if (block.getBlockData() instanceof org.bukkit.block.data.Levelled levelled) {
                        if (levelled.getLevel() == 0) {
                            if (block.getRelative(0, 1, 0).getType() == Material.AIR) {
                                block.setType(Material.ICE);
                            }
                        }
                    }
                }
            }
        }
    }
    public ItemStack criarEspadaGelo() {
        ItemStack espada = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = espada.getItemMeta();

        meta.displayName(Component.text("Espada de Gelo", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));

        //ID único para textura
        meta.setCustomModelData(1001);

        meta.lore(java.util.List.of(Component.text("Aplica lentidão aos inimigos!", NamedTextColor.GRAY)));

        espada.setItemMeta(meta);
        return espada;
    }

    @EventHandler
    public void aoAtacar(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            ItemStack item = player.getInventory().getItemInMainHand();

            // Verifica se o item tem o nosso CustomModelData
            if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
                if (item.getItemMeta().getCustomModelData() == 1001) {
                    if (event.getEntity() instanceof org.bukkit.entity.LivingEntity vitima) {
                        // Aplica lentidão nível 2 por 5 segundos (100 ticks)
                        vitima.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.SLOWNESS, 100, 1));

                        player.sendMessage(Component.text("Inimigo congelado!", NamedTextColor.AQUA));
                    }
                }
            }
        }
    }
}
