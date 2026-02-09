package jnetu.meu_plugin.habilidades;

import jnetu.meu_plugin.Meu_plugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class HabilidadesListener implements Listener {

    private final Meu_plugin plugin;
    private final HabilidadesManager manager;

    public HabilidadesListener(Meu_plugin plugin, HabilidadesManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void aoAndar(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PassoGeladoHabilidade passoGelado = (PassoGeladoHabilidade) manager.getHabilidade("passo-gelado");

        if (passoGelado == null || !passoGelado.estaAtivo(player)) return;

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
}
