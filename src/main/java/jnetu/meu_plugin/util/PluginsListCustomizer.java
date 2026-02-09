package jnetu.meu_plugin.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Arrays;
import java.util.List;

/**
 * Customiza a lista de plugins exibida pelo comando /plugins (ou /pl)
 * Bloquear completamente o comando /plugins
 */
public class PluginsListCustomizer implements Listener {

    private final JavaPlugin plugin;

    public PluginsListCustomizer(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Bloquear completamente o comando /plugins e enviar Lista Completamente Falsa
     */
    @EventHandler
    public void aoDigitarComando(PlayerCommandPreprocessEvent event) {
        String comando = event.getMessage().toLowerCase();
        // Detecta /plugins, /pl, /bukkit:plugins, /bukkit:pl
        if (comando.equals("/plugins") || comando.equals("/pl") ||
                comando.startsWith("/plugins ") || comando.startsWith("/pl ") ||
                comando.equals("/bukkit:plugins") || comando.equals("/bukkit:pl")) {
            Player player = event.getPlayer();
            event.setCancelled(true);
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
            return;
        }
    }

    /**
     * Falsificar completamente a lista
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
        player.sendMessage(Component.text("Este servidor roda em magia pura! ✦",
                NamedTextColor.LIGHT_PURPLE));
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