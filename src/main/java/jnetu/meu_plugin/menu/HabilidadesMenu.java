package jnetu.meu_plugin.menu;

import jnetu.meu_plugin.Meu_plugin;
import jnetu.meu_plugin.habilidades.Habilidade;
import jnetu.meu_plugin.habilidades.HabilidadesManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class HabilidadesMenu {

    private final Meu_plugin plugin;
    private final HabilidadesManager manager;
    public static final String TITULO = "Menu de Habilidades";

    public HabilidadesMenu(Meu_plugin plugin, HabilidadesManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void abrir(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text(TITULO));

        List<Habilidade> habilidades = new ArrayList<>(manager.getHabilidades().values());

        // Posiciona as habilidades no inventário
        int[] slots = {11, 12, 13, 14, 15}; // Slots centralizados

        for (int i = 0; i < Math.min(habilidades.size(), slots.length); i++) {
            Habilidade habilidade = habilidades.get(i);
            gui.setItem(slots[i], habilidade.getMenuItem(player));
        }

        player.openInventory(gui);
    }
}
