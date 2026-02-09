package jnetu.meu_plugin.comandos;

import jnetu.meu_plugin.Meu_plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SetHomeComando extends ComandoBase {

    public SetHomeComando(Meu_plugin plugin) {
        super(plugin);
    }

    @Override
    public String getNome() {
        return "sethome";
    }

    @Override
    public String getDescricao() {
        return "Define sua localização home";
    }

    @Override
    public String getPermissao() {
        return "meuplugin.sethome";
    }

    @Override
    public boolean executar(Player player, String[] args) {
        Location loc = player.getLocation();
        String uuid = player.getUniqueId().toString();

        plugin.getConfig().set("homes." + uuid + ".world", loc.getWorld().getName());
        plugin.getConfig().set("homes." + uuid + ".x", loc.getX());
        plugin.getConfig().set("homes." + uuid + ".y", loc.getY());
        plugin.getConfig().set("homes." + uuid + ".z", loc.getZ());
        plugin.getConfig().set("homes." + uuid + ".yaw", loc.getYaw());
        plugin.getConfig().set("homes." + uuid + ".pitch", loc.getPitch());
        plugin.saveConfig();

        player.sendMessage(Component.text("✓ Home definida!", NamedTextColor.GREEN));
        return true;
    }
}
