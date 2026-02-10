package jnetu.meu_plugin.itens;

import jnetu.meu_plugin.Meu_plugin;
import jnetu.meu_plugin.crafting.ItemFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ItensListener implements Listener {

    private final Meu_plugin plugin;
    private final ItensManager manager;

    public ItensListener(Meu_plugin plugin, ItensManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void aoInteragir(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        // Verifica se é Essência de Pedra usando o método atualizado
        if (ItemFactory.isEssenciaPedra(item)) {
            // Busca o item customizado correspondente
            ItemCustomizado itemCustomizado = manager.getItem(2002);
            if (itemCustomizado != null) {
                itemCustomizado.aoInteragir(player, event);
            }
        }

        // Verifica Custom Model Data para outros itens customizados
        if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {
            int customModelData = item.getItemMeta().getCustomModelData();
            ItemCustomizado itemCustomizado = manager.getItem(customModelData);

            if (itemCustomizado != null) {
                itemCustomizado.aoInteragir(player, event);
            }
        }
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
