package jnetu.meu_plugin.skill;

import dev.aurelium.auraskills.api.item.ItemContext;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.stat.CustomStat;

public class MinhasStats {
    public static final CustomStat CARISMA = CustomStat
            .builder(NamespacedId.of("meu_plugin", "carisma"))
            .displayName("Carisma")
            .description("Cada ponto reduz em 10% o tempo para recarregar a bateria social")
            .symbol("✦")
            .color("<green>")
            .trait(MinhasTraits.REDUCAO_BATERIA, 10.0)
            .item(ItemContext.builder()
                    .material("player_head")
                    .group("lower")
                    .order(2)
                    .build())
            .build();
}

