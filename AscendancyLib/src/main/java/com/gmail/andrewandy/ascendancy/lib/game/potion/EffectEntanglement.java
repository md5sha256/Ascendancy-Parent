package com.gmail.andrewandy.ascendancy.lib.game.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class EffectEntanglement extends PotionEffect {

    public EffectEntanglement(final Potion potionIn) {
        super(potionIn);
    }

    public EffectEntanglement(final Potion potionIn, final int durationIn) {
        super(potionIn, durationIn);
    }

    public EffectEntanglement(final Potion potionIn, final int durationIn, final int amplifierIn) {
        super(potionIn, durationIn, amplifierIn);
    }

    public EffectEntanglement(final Potion potionIn, final int durationIn, final int amplifierIn,
                              final boolean ambientIn, final boolean showParticlesIn) {
        super(potionIn, durationIn, amplifierIn, ambientIn, showParticlesIn);
    }

    public EffectEntanglement(final PotionEffect other) {
        super(other);
    }

    @Override public void performEffect(final EntityLivingBase entityIn) {
        super.performEffect(entityIn);
    }
}
