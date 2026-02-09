package jnetu.meu_plugin.habilidades;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Habilidade {

    /**
     * Identificador único da habilidade
     */
    String getId();

    /**
     * Nome exibido ao jogador
     */
    String getNome();

    /**
     * Descrição da habilidade
     */
    String getDescricao();

    /**
     * Ícone usado no menu
     */
    Material getIcone();

    /**
     * Ativa a habilidade para o jogador
     */
    void ativar(Player player);

    /**
     * Desativa a habilidade para o jogador
     */
    void desativar(Player player);

    /**
     * Verifica se a habilidade está ativa
     */
    boolean estaAtivo(Player player);

    /**
     * Retorna o ItemStack para o menu
     */
    ItemStack getMenuItem(Player player);
}
