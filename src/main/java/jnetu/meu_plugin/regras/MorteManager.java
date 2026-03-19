package jnetu.meu_plugin.regras;

import jnetu.meu_plugin.Meu_plugin;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Gerencia regras de morte: keep inventorytrue,
 * TODO - penalidades futuras, etc.
 */
public class MorteManager implements Listener {

    private final Meu_plugin plugin;

    public MorteManager(Meu_plugin plugin) {
        this.plugin = plugin;
        aplicarRegras();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void aplicarRegras() {
        for (World world : plugin.getServer().getWorlds()) {
            world.setGameRule(org.bukkit.GameRules.KEEP_INVENTORY, true);
        }
        plugin.getLogger().info("✅ Keep Inventory ativado!");
    }

    /**
     * Garante o keep inventory em mundos carregados depois do plugin
     */
    public void aplicarEmMundo(World world) {
        world.setGameRule(org.bukkit.GameRules.KEEP_INVENTORY, true);
    }


    @EventHandler
    public void aoMorrer(PlayerDeathEvent event) {
        // TODO - penas por morte (Vault vai remover parte do dinheiro futuramente)
    }
}