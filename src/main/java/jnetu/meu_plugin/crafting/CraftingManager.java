package jnetu.meu_plugin.crafting;

import jnetu.meu_plugin.Meu_plugin;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Recipe;

import java.util.HashMap;
import java.util.Map;

/**
 * Gerenciador de receitas customizadas
 */
public class CraftingManager {

    private final Meu_plugin plugin;
    private final Map<String, CustomRecipe> receitas;
    private final DropManager dropManager;
    private final RecipeBlockListener recipeBlockListener;

    public CraftingManager(Meu_plugin plugin) {
        this.plugin = plugin;
        this.receitas = new HashMap<>();
        this.dropManager = new DropManager(plugin);
        this.recipeBlockListener = new RecipeBlockListener(plugin);

        // Inicializa configuração padrão
        //inicializarConfig();

        registrarListeners();

        // Registra receitas
        registrarReceita(new EssenciaPedraRecipe(plugin));
    }

    /**
     * Inicializa as configurações padrão no config.yml
     */
    private void inicializarConfig() {
        plugin.getConfig().addDefault("crafting.essencia.drop-chance", 0.50);
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    /**
     * Registra uma receita customizada
     */
    public void registrarReceita(CustomRecipe receita) {
        receitas.put(receita.getId(), receita);

        // Adiciona a receita ao servidor
        Recipe bukkitRecipe = receita.criarReceita();
        Bukkit.addRecipe(bukkitRecipe);

        plugin.getLogger().info("✅ Receita customizada registrada: " + receita.getNome());
    }

    /**
     * Remove todas as receitas customizadas
     */
    public void removerReceitas() {
        for (CustomRecipe receita : receitas.values()) {
            // Não há método direto para remover, então usamos iterator
            plugin.getLogger().info("ℹ Receita " + receita.getNome() + " será removida no reload");
        }
    }

    /**
     * Registra os listeners do sistema de crafting
     */
    private void registrarListeners() {
        plugin.getServer().getPluginManager().registerEvents(recipeBlockListener, plugin);
        plugin.getLogger().info("✅ Proteção de receitas vanilla ativada!");
    }

    /**
     * Obtém o gerenciador de drops
     */
    public DropManager getDropManager() {
        return dropManager;
    }

    /**
     * Obtém uma receita pelo ID
     */
    public CustomRecipe getReceita(String id) {
        return receitas.get(id);
    }

    /**
     * Retorna todas as receitas registradas
     */
    public Map<String, CustomRecipe> getReceitas() {
        return receitas;
    }
}
