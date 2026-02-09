package jnetu.meu_plugin.comandos;

import jnetu.meu_plugin.Meu_plugin;
import org.bukkit.entity.Player;

public class HabilidadesComando extends ComandoBase {

    public HabilidadesComando(Meu_plugin plugin) {
        super(plugin);
    }

    @Override
    public String getNome() {
        return "habilidades";
    }

    @Override
    public String getDescricao() {
        return "Abre o menu de habilidades";
    }

    @Override
    public boolean executar(Player player, String[] args) {
        plugin.getHabilidadesManager().abrirMenu(player);
        return true;
    }
}
