package jnetu.meu_plugin.itens;

import jnetu.meu_plugin.Meu_plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class EspadaGeloItem implements ItemCustomizado {

    private final Meu_plugin plugin;

    public EspadaGeloItem(Meu_plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public int getCustomModelData() {
        return 1001;
    }

    @Override
    public String getNome() {
        return "Espada de Gelo";
    }

    @Override
    public ItemStack criar() {
        ItemStack espada = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = espada.getItemMeta();

        meta.displayName(Component.text("⚔ Espada de Gelo", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(List.of(
                Component.text("Uma lâmina congelante que", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("reduz a velocidade dos inimigos", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text(""),
                Component.text("❄ Habilidade: Congelamento", NamedTextColor.BLUE)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        meta.setCustomModelData(getCustomModelData());
        espada.setItemMeta(meta);

        return espada;
    }

    @Override
    public void aoAtacar(Player player, EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof LivingEntity vitima) {
            vitima.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOWNESS, 100, 1));
            player.sendMessage(Component.text("❄ Inimigo congelado!", NamedTextColor.AQUA));
        }
    }
}
