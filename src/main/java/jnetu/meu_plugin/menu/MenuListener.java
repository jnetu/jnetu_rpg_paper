package jnetu.meu_plugin.menu;

import jnetu.meu_plugin.Meu_plugin;
import jnetu.meu_plugin.habilidades.Habilidade;
import jnetu.meu_plugin.habilidades.HabilidadesManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MenuListener implements Listener {

    private final Meu_plugin plugin;
    private final HabilidadesManager manager;

    public MenuListener(Meu_plugin plugin, HabilidadesManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void aoClicarNoMenu(InventoryClickEvent event) {
        if (!event.getView().title().equals(Component.text(HabilidadesMenu.TITULO))) return;
        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();

        // Identifica qual habilidade foi clicada pelo material
        for (Habilidade habilidade : manager.getHabilidades().values()) {
            if (item.getType() == habilidade.getIcone()) {
                manager.alternarHabilidade(player, habilidade.getId());
                player.closeInventory();
                break;
            }
        }
    }
}
