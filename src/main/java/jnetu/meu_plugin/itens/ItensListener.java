package jnetu.meu_plugin.itens;

import jnetu.meu_plugin.Meu_plugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ItensListener implements Listener {

    private final Meu_plugin plugin;
    private final ItensManager manager;

    public ItensListener(Meu_plugin plugin, ItensManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void aoAtacar(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) return;

        int customModelData = item.getItemMeta().getCustomModelData();
        ItemCustomizado itemCustomizado = manager.getItem(customModelData);

        if (itemCustomizado != null) {
            itemCustomizado.aoAtacar(player, event);
        }
    }
}
