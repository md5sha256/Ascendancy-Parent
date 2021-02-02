package com.gmail.andrewandy.ascendency.client;

import com.gmail.andrewandy.ascendancy.lib.effect.AscendancyEffects;
import com.gmail.andrewandy.ascendency.client.io.ClientPacketHandler;
import com.gmail.andrewandy.ascendency.client.keybinds.ActiveKeyBind;
import net.minecraft.potion.Potion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = AscendancyClient.MOD_ID,
        name = AscendancyClient.MOD_NAME,
        version = AscendancyClient.VERSION
)
public class AscendancyClient {

    public static final String MOD_ID = "AscendancyCustomMod";
    public static final String MOD_NAME = "AscendancyCustomMod";
    public static final String VERSION = "2019.3-1.3.2";
    public static final String DATA_CHANNEL_NAME = "ASCENDANCY_DATA_CHANNEL";
    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static AscendancyClient INSTANCE;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            loadClient();
        } else {
            throw new UnsupportedOperationException("This is a client-side only mod!");
        }
    }


    private void loadClient() {
        ClientPacketHandler.getInstance().initForge();
        ClientRegistry.registerKeyBinding(ActiveKeyBind.INSTANCE.getKeyBinding());
        MinecraftForge.EVENT_BUS.register(ActiveKeyBind.INSTANCE);
    }


    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Potions {
        @SubscribeEvent
        public void registerPotions(RegistryEvent.Register<Potion> potionRegistryEvent) {
            potionRegistryEvent.getRegistry().registerAll(AscendancyEffects.POTIONS.toArray(new Potion[0]));
        }
    }

}
