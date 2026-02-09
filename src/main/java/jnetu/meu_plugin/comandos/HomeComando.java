package jnetu.meu_plugin.comandos;

import jnetu.meu_plugin.Meu_plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class HomeComando extends ComandoBase {

    public HomeComando(Meu_plugin plugin) {
        super(plugin);
    }

    @Override
    public String getNome() {
        return "home";
    }

    @Override
    public String getDescricao() {
        return "Teleporta você para sua home";
    }

    @Override
    public String getPermissao() {
        return "meuplugin.home";
    }

    @Override
    public boolean executar(Player player, String[] args) {
        String uuid = player.getUniqueId().toString();

        if (!plugin.getConfig().contains("homes." + uuid)) {
            player.sendMessage(Component.text("❌ Você não tem home! Use /sethome.",
                    NamedTextColor.RED));
            return true;
        }

        String world = plugin.getConfig().getString("homes." + uuid + ".world");
        double x = plugin.getConfig().getDouble("homes." + uuid + ".x");
        double y = plugin.getConfig().getDouble("homes." + uuid + ".y");
        double z = plugin.getConfig().getDouble("homes." + uuid + ".z");
        float yaw = (float) plugin.getConfig().getDouble("homes." + uuid + ".yaw");
        float pitch = (float) plugin.getConfig().getDouble("homes." + uuid + ".pitch");

        Location home = new Location(plugin.getServer().getWorld(world), x, y, z, yaw, pitch);
        player.teleport(home);
        player.sendMessage(Component.text("✓ Teleportado para sua home!", NamedTextColor.AQUA));
        return true;
    }
}
