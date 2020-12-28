package com.gmail.andrewandy.ascendancy.lib.game.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.potion.Potion;

public class PotionEntanglement extends Potion {

    public PotionEntanglement(final boolean isBadEffectIn, final int liquidColorIn) {
        super(isBadEffectIn, liquidColorIn);
    }

    @Override
    public void performEffect(final EntityLivingBase entityLivingBaseIn, final int p_76394_2_) {
        super.performEffect(entityLivingBaseIn, p_76394_2_);
    }

    @Override
    public Potion registerPotionAttributeModifier(final IAttribute attribute, final String uniqueId,
                                                  final double ammount, final int operation) {
        return super.registerPotionAttributeModifier(attribute, uniqueId, ammount, operation);
    }
}
