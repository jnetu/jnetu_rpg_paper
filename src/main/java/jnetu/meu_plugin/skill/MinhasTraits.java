package jnetu.meu_plugin.skill;

import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.trait.CustomTrait;

public class MinhasTraits {

    public static final CustomTrait REDUCAO_BATERIA = CustomTrait
            .builder(
                    NamespacedId.of(
                            "meu_plugin",
                            "chat_battery"))
            .displayName("Redução de Recarga Social")
            .build();
}
