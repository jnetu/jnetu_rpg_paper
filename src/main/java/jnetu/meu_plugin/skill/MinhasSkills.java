package jnetu.meu_plugin.skill;

import dev.aurelium.auraskills.api.item.ItemContext;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.CustomSkill;

public class MinhasSkills {
    public static final CustomSkill SOCIAL = CustomSkill.builder(NamespacedId.of("meu_plugin", "social"))
            .displayName("Social")
            .description("Converse para ganhar XP. Quanto mais tempo sem falar, mais XP!")
            .item(ItemContext.builder()
                    .material("player_head")
                    .pos("4,4")
                    .build())
            .build();
    public static final CustomSkill MARKENTING = CustomSkill.builder(NamespacedId.of("meu_plugin", "marketing"))
            .displayName("marketing")
            .description("galera do marketing ae")
            .item(ItemContext.builder()
                    .material("stone")
                    .pos("4,5")
                    .build())
            .build();
}
