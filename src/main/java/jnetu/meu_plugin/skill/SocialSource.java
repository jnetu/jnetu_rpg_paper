package jnetu.meu_plugin.skill;

import dev.aurelium.auraskills.api.source.CustomSource;
import dev.aurelium.auraskills.api.source.SourceValues;

public class SocialSource extends CustomSource {

    private final long rechargeMs;

    public SocialSource(SourceValues values, long rechargeSeconds) {
        super(values);
        this.rechargeMs = rechargeSeconds * 1000;
    }

    public long getRechargeMs() {

        return rechargeMs;
    }
}
