package jnetu.meu_plugin.habilidades;

import jnetu.meu_plugin.Meu_plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class PassoGeladoHabilidade implements Habilidade {

    private final Meu_plugin plugin;
    private final HashSet<UUID> jogadoresAtivos;

    public PassoGeladoHabilidade(Meu_plugin plugin) {
        this.plugin = plugin;
        this.jogadoresAtivos = new HashSet<>();
    }

    @Override
    public String getId() {
        return "passo-gelado";
    }

    @Override
    public String getNome() {
        return "Passo Gelado";
    }

    @Override
    public String getDescricao() {
        return "Congela a água ao andar sobre ela";
    }

    @Override
    public Material getIcone() {
        return Material.ICE;
    }

    @Override
    public void ativar(Player player) {
        jogadoresAtivos.add(player.getUniqueId());
        player.sendMessage(Component.text("Passo Gelado ", NamedTextColor.AQUA)
                .append(Component.text("ATIVADO", NamedTextColor.GREEN)));
    }

    @Override
    public void desativar(Player player) {
        jogadoresAtivos.remove(player.getUniqueId());
        player.sendMessage(Component.text("Passo Gelado ", NamedTextColor.AQUA)
                .append(Component.text("DESATIVADO", NamedTextColor.RED)));
    }

    @Override
    public boolean estaAtivo(Player player) {
        return jogadoresAtivos.contains(player.getUniqueId());
    }

    @Override
    public ItemStack getMenuItem(Player player) {
        ItemStack item = new ItemStack(getIcone());
        ItemMeta meta = item.getItemMeta();

        boolean ativo = estaAtivo(player);

        meta.displayName(Component.text(getNome(), NamedTextColor.AQUA));
        meta.lore(List.of(
                Component.text(getDescricao(), NamedTextColor.GRAY),
                Component.text(""),
                Component.text("Status: " + (ativo ? "ATIVADO" : "DESATIVADO"),
                        ativo ? NamedTextColor.GREEN : NamedTextColor.RED)
        ));

        item.setItemMeta(meta);
        return item;
    }

    public HashSet<UUID> getJogadoresAtivos() {
        return jogadoresAtivos;
    }
}
