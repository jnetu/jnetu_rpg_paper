package jnetu.meu_plugin.itens;

import jnetu.meu_plugin.Meu_plugin;

import java.util.HashMap;
import java.util.Map;

public class ItensManager {

    private final Meu_plugin plugin;
    private final Map<Integer, ItemCustomizado> itensCustomizados;

    public ItensManager(Meu_plugin plugin) {
        this.plugin = plugin;
        this.itensCustomizados = new HashMap<>();

        // Registra itens customizados
        registrarItem(new EspadaGeloItem(plugin));
    }

    public void registrarItem(ItemCustomizado item) {
        itensCustomizados.put(item.getCustomModelData(), item);
        plugin.getLogger().info("✅ Item customizado registrado: " + item.getNome() + " (CMD: " + item.getCustomModelData() + ")");
    }

    public void registrarListeners() {
        plugin.getServer().getPluginManager().registerEvents(
                new ItensListener(plugin, this),
                plugin
        );
    }

    public ItemCustomizado getItem(int customModelData) {
        return itensCustomizados.get(customModelData);
    }

    public Map<Integer, ItemCustomizado> getItensCustomizados() {
        return itensCustomizados;
    }
}
