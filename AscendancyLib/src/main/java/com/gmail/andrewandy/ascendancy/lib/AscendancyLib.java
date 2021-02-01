package com.gmail.andrewandy.ascendancy.lib;

import com.gmail.andrewandy.ascendancy.lib.effect.AscendancyEffects;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AscendancyLib {

    @SubscribeEvent
    public void registerPotions(RegistryEvent.Register<Potion> potionRegistryEvent) {
        potionRegistryEvent.getRegistry().registerAll(AscendancyEffects.POTIONS.toArray(new Potion[0]));
    }

}
