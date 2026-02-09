package jnetu.meu_plugin.comandos;

import jnetu.meu_plugin.Meu_plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ComandoBase implements CommandExecutor, TabCompleter {

    protected final Meu_plugin plugin;

    public ComandoBase(Meu_plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Nome do comando (sem a barra)
     */
    public abstract String getNome();

    /**
     * Descrição do comando
     */
    public abstract String getDescricao();

    /**
     * Permissão necessária (null = sem permissão)
     */
    public String getPermissao() {
        return null;
    }

    /**
     * Se o comando requer que o executor seja um jogador
     */
    public boolean requerJogador() {
        return true;
    }

    /**
     * Executa o comando para jogadores
     */
    public abstract boolean executar(Player player, String[] args);

    /**
     * Executa o comando para console (apenas se requerJogador() == false)
     */
    public boolean executarConsole(CommandSender sender, String[] args) {
        sender.sendMessage("Este comando só pode ser executado por jogadores.");
        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        // Verifica se requer jogador
        if (requerJogador() && !(sender instanceof Player)) {
            return executarConsole(sender, args);
        }

        // Verifica permissão
        if (getPermissao() != null && !sender.hasPermission(getPermissao())) {
            sender.sendMessage(Component.text("❌ Você não tem permissão para usar este comando!",
                    NamedTextColor.RED));
            return true;
        }

        // Executa o comando
        if (sender instanceof Player player) {
            return executar(player, args);
        } else {
            return executarConsole(sender, args);
        }
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        return null; // Subclasses podem sobrescrever para autocompletar
    }
}
