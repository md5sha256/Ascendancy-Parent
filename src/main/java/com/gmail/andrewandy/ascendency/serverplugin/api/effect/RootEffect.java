package com.gmail.andrewandy.ascendency.serverplugin.api.effect;

import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;

public class RootEffect extends AbstractPotionEffect implements PotionEffect {

    public RootEffect(int duration, int amplifier, boolean showEffects) {
        super(Type.instance, duration, amplifier, false, showEffects);
    }

    public static void registerHandler(@NotNull final AscendencyServerPlugin plugin) {
        Sponge.getEventManager().registerListeners(new Handler(), plugin);
    }

    @Override public int getContentVersion() {
        return 0;
    }

    @Override @NotNull public DataContainer toContainer() {
        return null;
    } //TODO


    private static class Handler {
        Handler() {
        }

        @Listener(order = Order.FIRST) public void onMove(final MoveEntityEvent event) {
            event.getTargetEntity().getOrCreate(PotionEffectData.class).ifPresent(data -> {
                ListValue<PotionEffect> effects = data.effects();
                for (PotionEffect effect : effects) {
                    if (effect instanceof RootEffect) {
                        event.setCancelled(true);
                        return;
                    }
                }
            });
        }
    }


    public static class Type extends AbstractPotionEffectType {

        public static final Type instance = new Type();

        private Type() {
            super("RootEffect", "ascendencyserverplugin:root_effect", true);
        }

    }


}
