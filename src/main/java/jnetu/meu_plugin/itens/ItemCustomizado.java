package jnetu.meu_plugin.itens;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public interface ItemCustomizado {

    /**
     * ID único do Custom Model Data
     */
    int getCustomModelData();

    /**
     * Nome do item
     */
    String getNome();

    /**
     * Cria o ItemStack do item customizado
     */
    ItemStack criar();

    /**
     * Executado quando o jogador ataca com o item
     */
    default void aoAtacar(Player player, EntityDamageByEntityEvent event) {}

    /**
     * Executado quando o jogador interage com o item
     */
    default void aoInteragir(Player player, PlayerInteractEvent event) {}
}
