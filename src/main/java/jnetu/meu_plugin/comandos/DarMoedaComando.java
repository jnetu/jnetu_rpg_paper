package jnetu.meu_plugin.comandos;

import jnetu.meu_plugin.Meu_plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DarMoedaComando extends ComandoBase {

    public DarMoedaComando(Meu_plugin plugin) {
        super(plugin);
    }

    @Override
    public String getNome() {
        return "darmoeda";
    }

    @Override
    public String getDescricao() {
        return "Dá moedas para você (admin)";
    }

    @Override
    public String getPermissao() {
        return "meuplugin.darmoeda";
    }

    @Override
    public boolean executar(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(Component.text("❌ Use: /darmoeda <quantia>", NamedTextColor.RED));
            return true;
        }

        try {
            double quantia = Double.parseDouble(args[0]);

            if (quantia <= 0) {
                player.sendMessage(Component.text("❌ A quantia deve ser maior que zero!",
                        NamedTextColor.RED));
                return true;
            }

            var economyReg = plugin.getServer().getServicesManager().getRegistration(Economy.class);

            if (economyReg == null) {
                player.sendMessage(Component.text("❌ Economia indisponível!", NamedTextColor.RED));
                return true;
            }

            Economy economy = economyReg.getProvider();
            economy.depositPlayer(player, quantia);

            player.sendMessage(Component.text("✓ Você recebeu ", NamedTextColor.GREEN)
                    .append(Component.text(String.format("%.2f Moedas!", quantia),
                            NamedTextColor.GOLD)));

        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("❌ Valor inválido! Use um número.",
                    NamedTextColor.RED));
        }
        return true;
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> sugestoes = new ArrayList<>();
            sugestoes.add("100");
            sugestoes.add("500");
            sugestoes.add("1000");
            sugestoes.add("5000");
            return sugestoes;
        }
        return null;
    }
}
