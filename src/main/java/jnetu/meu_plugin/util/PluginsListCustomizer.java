package jnetu.meu_plugin.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Customiza a lista de plugins exibida pelo comando /plugins (ou /pl)
 *
 * Opções:
 * 1. Bloquear completamente o comando
 * 2. Ocultar plugins específicos
 * 3. Criar lista customizada
 * 4. Apenas para admins
 */
public class PluginsListCustomizer implements Listener {

    private final JavaPlugin plugin;

    // Plugins que você quer OCULTAR da lista
    private final List<String> pluginsOcultos = Arrays.asList(
            "ProtocolLib",
            "Vault",
            "LuckPerms",
            "AuraSkills"
            // Adicione mais aqui
    );

    // Plugins que você quer SEMPRE MOSTRAR (mesmo se não estiverem instalados - fake)
    private final List<String> pluginsFalsos = Arrays.asList(
            "SuperPlugin",
            "MegaProtecao"
            // Adicione plugins fake aqui
    );

    public PluginsListCustomizer(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * OPÇÃO 1: Bloquear completamente o comando /plugins
     * Apenas admins com permissão podem ver
     */
    @EventHandler
    public void aoDigitarComando(PlayerCommandPreprocessEvent event) {
        String comando = event.getMessage().toLowerCase();

        // Detecta /plugins, /pl, /bukkit:plugins, /bukkit:pl
        if (comando.equals("/plugins") || comando.equals("/pl") ||
                comando.startsWith("/plugins ") || comando.startsWith("/pl ") ||
                comando.equals("/bukkit:plugins") || comando.equals("/bukkit:pl")) {

            Player player = event.getPlayer();

            // MODO 1: Bloquear para todos exceto admins
//            if (!player.hasPermission("meuplugin.ver.plugins")) {
//                event.setCancelled(true);
//                player.sendMessage(Component.text("Comando desconhecido. Digite \"/help\" para ajuda.",
//                        NamedTextColor.RED));
//                return;
//            }

            // MODO 2: Mostrar lista customizada
            event.setCancelled(true);
            //enviarListaCustomizada(player);
            enviarListaCompletamenteFalsa(player);
        }
    }

    /**
     * Intercepta comando do console também
     */
    @EventHandler
    public void aoDigitarComandoConsole(ServerCommandEvent event) {
        String comando = event.getCommand().toLowerCase();

        if (comando.equals("plugins") || comando.equals("pl")) {
            // Console sempre pode ver tudo
            // Mas você pode customizar se quiser
        }
    }

    /**
     * Envia uma lista TOTALMENTE CUSTOMIZADA de plugins
     */
    private void enviarListaCustomizada(CommandSender sender) {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();

        // Filtra plugins ocultos
        List<String> pluginsVisiveis = Arrays.stream(plugins)
                .map(Plugin::getName)
                .filter(nome -> !pluginsOcultos.contains(nome))
                .collect(Collectors.toList());

        // Adiciona plugins falsos
        pluginsVisiveis.addAll(pluginsFalsos);

        // Ordena alfabeticamente
        pluginsVisiveis.sort(String.CASE_INSENSITIVE_ORDER);

        // Monta a mensagem estilo vanilla
        Component mensagem = Component.text("Plugins (", NamedTextColor.WHITE)
                .append(Component.text(pluginsVisiveis.size(), NamedTextColor.YELLOW))
                .append(Component.text("): ", NamedTextColor.WHITE));

        // Adiciona cada plugin com cor
        for (int i = 0; i < pluginsVisiveis.size(); i++) {
            String nomePlugin = pluginsVisiveis.get(i);

            // Verde se estiver ativo (real), cinza se for falso
            NamedTextColor cor = pluginsFalsos.contains(nomePlugin)
                    ? NamedTextColor.GRAY
                    : NamedTextColor.GREEN;

            mensagem = mensagem.append(Component.text(nomePlugin, cor));

            // Adiciona vírgula se não for o último
            if (i < pluginsVisiveis.size() - 1) {
                mensagem = mensagem.append(Component.text(", ", NamedTextColor.WHITE));
            }
        }

        sender.sendMessage(mensagem);
    }

    /**
     * ALTERNATIVA: Criar comando próprio /mpluginslist
     */
    public static class ComandoPluginsCustomizado implements CommandExecutor {

        private final JavaPlugin plugin;

        public ComandoPluginsCustomizado(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, @NotNull String[] args) {

            // Apenas admins
            if (sender instanceof Player player) {
                if (!player.hasPermission("meuplugin.admin")) {
                    player.sendMessage(Component.text("Sem permissão!", NamedTextColor.RED));
                    return true;
                }
            }

            // Lista completa e detalhada
            Plugin[] plugins = Bukkit.getPluginManager().getPlugins();

            sender.sendMessage(Component.text("========================================",
                    NamedTextColor.GOLD, TextDecoration.BOLD));
            sender.sendMessage(Component.text(" Lista de Plugins do Servidor",
                    NamedTextColor.YELLOW, TextDecoration.BOLD));
            sender.sendMessage(Component.text("========================================",
                    NamedTextColor.GOLD, TextDecoration.BOLD));
            sender.sendMessage(Component.empty());

            for (Plugin p : plugins) {
                Component linha = Component.text("• ", NamedTextColor.GRAY)
                        .append(Component.text(p.getName(),
                                p.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED))
                        .append(Component.text(" v" + p.getDescription().getVersion(),
                                NamedTextColor.DARK_GRAY))
                        .append(Component.text(" - ", NamedTextColor.GRAY))
                        .append(Component.text(
                                p.isEnabled() ? "✓ Ativo" : "✗ Desativado",
                                p.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED));

                sender.sendMessage(linha);
            }

            sender.sendMessage(Component.empty());
            sender.sendMessage(Component.text("Total: " + plugins.length + " plugins",
                    NamedTextColor.YELLOW));

            return true;
        }
    }

    /**
     * MODO HARDCORE: Falsificar completamente a lista
     * Mostra APENAS plugins fake
     */
    private void enviarListaCompletamenteFalsa(CommandSender sender) {
        List<String> pluginsInventados = Arrays.asList(
                "Piauicraft", // Fake - não está instalado
                "Piauicraft-Protegido"  // Fake - não está instalado
        );

        Component mensagem = Component.text("Plugins (", NamedTextColor.WHITE)
                .append(Component.text(pluginsInventados.size(), NamedTextColor.YELLOW))
                .append(Component.text("): ", NamedTextColor.WHITE));

        for (int i = 0; i < pluginsInventados.size(); i++) {
            mensagem = mensagem.append(Component.text(pluginsInventados.get(i),
                    NamedTextColor.GREEN));

            if (i < pluginsInventados.size() - 1) {
                mensagem = mensagem.append(Component.text(", ", NamedTextColor.WHITE));
            }
        }

        sender.sendMessage(mensagem);
    }

    /**
     * MODO TROLLAGEM: Mostrar mensagem engraçada
     */
    private void trollarPlayer(Player player) {
        player.sendMessage(Component.text("Plugins? Que plugins?", NamedTextColor.LIGHT_PURPLE));
        player.sendMessage(Component.text("Este servidor roda em magia pura! ✨",
                NamedTextColor.LIGHT_PURPLE));
    }

    /**
     * MODO PROFISSIONAL: Lista categorizada
     */
    private void enviarListaCategorizada(CommandSender sender) {
        sender.sendMessage(Component.text("========================================",
                NamedTextColor.GOLD));
        sender.sendMessage(Component.text(" Plugins do Servidor", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("========================================",
                NamedTextColor.GOLD));
        sender.sendMessage(Component.empty());

        // Categoria: Core
        sender.sendMessage(Component.text("⚙ Core:", NamedTextColor.AQUA, TextDecoration.BOLD));
        sender.sendMessage(Component.text("  • Paper 1.21.4", NamedTextColor.WHITE));
        sender.sendMessage(Component.empty());

        // Categoria: Gameplay
        sender.sendMessage(Component.text("🎮 Gameplay:", NamedTextColor.GREEN, TextDecoration.BOLD));
        sender.sendMessage(Component.text("  • AuraSkills", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("  • MeuPlugin (Social System)", NamedTextColor.WHITE));
        sender.sendMessage(Component.empty());

        // Categoria: Proteção
        sender.sendMessage(Component.text("🛡 Segurança:", NamedTextColor.RED, TextDecoration.BOLD));
        sender.sendMessage(Component.text("  • [Protegido]", NamedTextColor.DARK_GRAY));
        sender.sendMessage(Component.empty());

        // Categoria: Utilitários
        sender.sendMessage(Component.text("🔧 Utilitários:", NamedTextColor.YELLOW, TextDecoration.BOLD));
        sender.sendMessage(Component.text("  • [Interno]", NamedTextColor.DARK_GRAY));
        sender.sendMessage(Component.empty());

        sender.sendMessage(Component.text("Total: 4 categorias visíveis",
                NamedTextColor.GRAY));
    }



    //evita hackers de tab
    @EventHandler
    public void aoUsarTabComplete(TabCompleteEvent event) {
        if (event.getBuffer().toLowerCase().startsWith("/plugins") ||
                event.getBuffer().toLowerCase().startsWith("/pl")) {

            // Bloqueia autocompletar
            event.setCancelled(true);
        }
    }
}
