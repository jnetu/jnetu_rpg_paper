package jnetu.meu_plugin;

import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Meu_plugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Plugin de jnetu iniciado com sucesso!");
    }

    public void onDisable() {
    }

    @EventHandler
    public void aoEntrar(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        //mensagem só para o player
        player.sendMessage(Component.text("Bem-vindo ao servidor.", NamedTextColor.GREEN));
        //mensagem global
        Component mensagemGlobal = Component.text("O jogador ", NamedTextColor.GRAY)
                .append(player.name().color(NamedTextColor.YELLOW))
                .append(Component.text(" acabou de entrar!", NamedTextColor.GRAY));

        Bukkit.broadcast(mensagemGlobal);
    }
}
