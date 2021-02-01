package com.gmail.andrewandy.ascendancy.lib.effect;

import net.minecraft.entity.EntityLivingBase;

public class EffectEntanglement extends AscendancyEffect {

    public EffectEntanglement(int duration, int amplifier) {
        super(AscendancyEffects.EFFECT_ENTANGLEMENT, duration, amplifier);
    }

    @Override
    public void applyEffect(EntityLivingBase entityliving) {
    }

    @Override
    public void stopEffect(EntityLivingBase entityliving) {
    }

    @Override
    public void performEffect(EntityLivingBase entityliving) {
        entityliving.motionX = 0f;
        entityliving.motionY = 0f;
        entityliving.motionZ = 0f;
    }

    @Override
    protected String spellBuffName() {
        return "Entangled";
    }

}
