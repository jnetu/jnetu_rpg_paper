package jnetu.meu_plugin.crafting;

import jnetu.meu_plugin.Meu_plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Drop customizado de Essência ao minerar pedra lisa
 */
public class EssenciaDropListener implements Listener, CustomDrop {

    private final Meu_plugin plugin;
    private double dropChance;

    public EssenciaDropListener(Meu_plugin plugin) {
        this.plugin = plugin;
        this.dropChance = plugin.getConfig().getDouble("crafting.essencia.drop-chance");
    }

    @Override
    public Material getBlockType() {
        return Material.STONE;
    }

    @Override
    public double getChance() {
        return dropChance;
    }

    @Override
    public List<ItemStack> getDrops() {
        return List.of(ItemFactory.criarEssencia());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void aoQuebrarBloco(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        Player player = event.getPlayer();

        // Verifica se deve dropar
        if (!shouldDrop(block, player)) return;

        // Dropa a Essência
        ItemStack essencia = ItemFactory.criarEssencia();
        onDrop(event, essencia);

        // Feedback visual e sonoro
        player.sendMessage(Component.text("✦ ", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text("Você encontrou uma ", NamedTextColor.GRAY))
                .append(Component.text("Essência", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("!", NamedTextColor.GRAY)));

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);

        // Log para debug
        plugin.getLogger().info(player.getName() + " encontrou uma Essência! (Chance: " +
                String.format("%.1f%%", dropChance * 100) + ")");
    }

    /**
     * Atualiza a chance de drop
     */
    public void setDropChance(double chance) {
        this.dropChance = Math.max(0.0, Math.min(1.0, chance));
        plugin.getConfig().set("crafting.essencia.drop-chance", this.dropChance);
        plugin.saveConfig();
    }

    /**
     * Recarrega a configuração
     */
    public void recarregarConfig() {
        this.dropChance = plugin.getConfig().getDouble("crafting.essencia.drop-chance");
    }
}
