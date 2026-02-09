package jnetu.meu_plugin.habilidades;

import jnetu.meu_plugin.Meu_plugin;
import jnetu.meu_plugin.menu.HabilidadesMenu;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HabilidadesManager {

    private final Meu_plugin plugin;
    private final Map<String, Habilidade> habilidades;
    private final HabilidadesMenu menu;

    public HabilidadesManager(Meu_plugin plugin) {
        this.plugin = plugin;
        this.habilidades = new HashMap<>();
        this.menu = new HabilidadesMenu(plugin, this);

        // Registra habilidades
        registrarHabilidade(new PassoGeladoHabilidade(plugin));
    }

    public void registrarHabilidade(Habilidade habilidade) {
        habilidades.put(habilidade.getId(), habilidade);
        plugin.getLogger().info("✅ Habilidade registrada: " + habilidade.getNome());
    }

    public void registrarListeners() {
        plugin.getServer().getPluginManager().registerEvents(
                new HabilidadesListener(plugin, this),
                plugin
        );
    }

    public void carregarEstadoJogador(Player player) {
        UUID uuid = player.getUniqueId();
        for (Habilidade habilidade : habilidades.values()) {
            if (plugin.getConfig().getBoolean("habilidades." + uuid + "." + habilidade.getId())) {
                habilidade.ativar(player);
            }
        }
    }

    public void alternarHabilidade(Player player, String habilidadeId) {
        Habilidade habilidade = habilidades.get(habilidadeId);
        if (habilidade == null) return;

        UUID uuid = player.getUniqueId();

        if (habilidade.estaAtivo(player)) {
            habilidade.desativar(player);
            plugin.getConfig().set("habilidades." + uuid + "." + habilidadeId, false);
        } else {
            habilidade.ativar(player);
            plugin.getConfig().set("habilidades." + uuid + "." + habilidadeId, true);
        }

        plugin.saveConfig();
    }

    public void abrirMenu(Player player) {
        menu.abrir(player);
    }

    public Map<String, Habilidade> getHabilidades() {
        return habilidades;
    }

    public Habilidade getHabilidade(String id) {
        return habilidades.get(id);
    }
}
