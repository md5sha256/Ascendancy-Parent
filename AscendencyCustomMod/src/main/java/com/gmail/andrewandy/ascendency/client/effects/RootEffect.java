package com.gmail.andrewandy.ascendency.client.effects;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class RootEffect extends Potion {


    public RootEffect() {
        super(true, 0);
    }



    public static class Type extends PotionEffect {

        public Type(final Potion potionIn) {
            super(potionIn);
        }

        public Type(final Potion potionIn, final int durationIn) {
            super(potionIn, durationIn);
        }

        public Type(final Potion potionIn, final int durationIn, final int amplifierIn) {
            super(potionIn, durationIn, amplifierIn);
        }

        public Type(final Potion potionIn, final int durationIn, final int amplifierIn,
            final boolean ambientIn, final boolean showParticlesIn) {
            super(potionIn, durationIn, amplifierIn, ambientIn, showParticlesIn);
        }

        public Type(final PotionEffect other) {
            super(other);
        }
    }
}
