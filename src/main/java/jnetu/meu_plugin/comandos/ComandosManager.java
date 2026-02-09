package jnetu.meu_plugin.comandos;

import jnetu.meu_plugin.Meu_plugin;
import org.bukkit.command.PluginCommand;

import java.util.HashMap;
import java.util.Map;

public class ComandosManager {

    private final Meu_plugin plugin;
    private final Map<String, ComandoBase> comandos;

    public ComandosManager(Meu_plugin plugin) {
        this.plugin = plugin;
        this.comandos = new HashMap<>();

        // Registra todos os comandos
        registrarComando(new SetHomeComando(plugin));
        registrarComando(new HomeComando(plugin));
        registrarComando(new HabilidadesComando(plugin));
        registrarComando(new BalanceComando(plugin));
        registrarComando(new DarMoedaComando(plugin));

        //adicionar outros...
        //registrarComando(new MeuNovoComando(plugin));
        //OBS: adicionar no plugins.yml commands.novocomando
    }

    /**
     * Registra um comando no plugin
     */
    public void registrarComando(ComandoBase comando) {
        String nome = comando.getNome().toLowerCase();

        PluginCommand pluginCommand = plugin.getCommand(nome);
        if (pluginCommand == null) {
            plugin.getLogger().warning("⚠ Comando '/" + nome + "' não está no plugin.yml!");
            return;
        }

        pluginCommand.setExecutor(comando);
        pluginCommand.setTabCompleter(comando);
        comandos.put(nome, comando);

        plugin.getLogger().info("✅ Comando registrado: /" + nome);
    }

    /**
     * Obtém um comando registrado
     */
    public ComandoBase getComando(String nome) {
        return comandos.get(nome.toLowerCase());
    }

    /**
     * Retorna todos os comandos registrados
     */
    public Map<String, ComandoBase> getComandos() {
        return comandos;
    }
}
