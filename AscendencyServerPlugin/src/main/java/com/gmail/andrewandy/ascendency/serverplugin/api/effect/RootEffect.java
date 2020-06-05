package com.gmail.andrewandy.ascendency.serverplugin.api.effect;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.effect.potion.PotionEffect;

public class RootEffect extends AbstractPotionEffect implements PotionEffect {

    public RootEffect(int duration, int amplifier, boolean showEffects) {
        super(Type.instance, duration, amplifier, false, showEffects);
    }

    @Override public int getContentVersion() {
        return 0;
    }

    @Override @NotNull public DataContainer toContainer() {
        return null;
    }


    public static class Type extends AbstractPotionEffectType {

        public static final Type instance = new Type();

        private Type() {
            super("RootEffect", "ascendencyserverplugin:root_effect", true);
        }

    }


}
