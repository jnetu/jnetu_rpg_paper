package jnetu.meu_plugin.comandos;

import jnetu.meu_plugin.Meu_plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class BalanceComando extends ComandoBase {

    public BalanceComando(Meu_plugin plugin) {
        super(plugin);
    }

    @Override
    public String getNome() {
        return "balance";
    }

    @Override
    public String getDescricao() {
        return "Mostra seu saldo de moedas";
    }

    @Override
    public boolean executar(Player player, String[] args) {
        var economyReg = plugin.getServer().getServicesManager().getRegistration(Economy.class);

        if (economyReg == null) {
            player.sendMessage(Component.text("❌ Economia indisponível!", NamedTextColor.RED));
            return true;
        }

        Economy economy = economyReg.getProvider();
        double saldo = economy.getBalance(player);

        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GRAY));
        player.sendMessage(Component.text("💰 Seu Saldo", NamedTextColor.GOLD, TextDecoration.BOLD));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("  Moedas: ", NamedTextColor.YELLOW)
                .append(Component.text(String.format("%.2f", saldo), NamedTextColor.WHITE)));
        player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━", NamedTextColor.GRAY));
        return true;
    }
}
