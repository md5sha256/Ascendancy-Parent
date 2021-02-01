package com.gmail.andrewandy.ascendancy.lib.effect;

import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.HashSet;

public class AscendancyEffects {

    public static final Collection<Potion> POTIONS = new HashSet<>();

    public static final Potion EFFECT_ENTANGLEMENT = createPotion(new ResourceLocation("ascendancy:entanglement"), true, 0x5a4b1e);

    private AscendancyEffects() {

    }

    public static Potion createPotion(ResourceLocation loc, boolean isBad, int color) {
        Potion potion = new AscendancyPotion(isBad, color).setPotionName(loc.toString());
        POTIONS.add(potion);
        return potion;
    }

}
