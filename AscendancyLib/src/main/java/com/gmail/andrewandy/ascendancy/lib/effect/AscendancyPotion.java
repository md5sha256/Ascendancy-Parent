package com.gmail.andrewandy.ascendancy.lib.effect;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class AscendancyPotion extends Potion {


    public AscendancyPotion(boolean badEffect, int potionColor) {
        super(badEffect, potionColor);
        if (!badEffect) this.setBeneficial();
    }

    @Override
    public boolean shouldRender(PotionEffect effect) {
        return !(effect instanceof AscendancyEffect);
    }
}
