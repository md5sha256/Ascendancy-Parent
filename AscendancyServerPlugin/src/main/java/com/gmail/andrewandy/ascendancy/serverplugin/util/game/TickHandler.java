package com.gmail.andrewandy.ascendancy.serverplugin.util.game;

import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.Sponge;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class TickHandler {

    private final Map<Tickable, TickData> toTick = new HashMap<>();

    @Inject
    TickHandler(AscendancyServerPlugin plugin) {
        Sponge.getScheduler().createTaskBuilder().execute(this::run).intervalTicks(1)
                .submit(plugin);
    }

    public void removeTickable(final Tickable tickable) {
        toTick.remove(tickable);
    }

    public void submitTickable(final Tickable tickable) {
        toTick.remove(tickable);
        submitTickable(tickable, 1);
    }

    public void submitTickable(final Tickable tickable, final int ticks) {
        toTick.remove(tickable);
        toTick.put(tickable, new TickData(tickable, ticks));
    }

    public void run() {
        toTick.values().removeIf(TickData::failedTick);
    }

    private static class TickData {

        private final Tickable tickable;
        private int remainingTicks;

        public TickData(Tickable tickable, int remainingTicks) {
            this.tickable = tickable;
            this.remainingTicks = remainingTicks;
        }

        public int getRemainingTicks() {
            return remainingTicks;
        }

        public Tickable getTickable() {
            return tickable;
        }

        /**
         * Ticks the underlying tickable object
         *
         * @return Returns whether or not the object was ticked.
         */
        public boolean failedTick() {
            switch (remainingTicks) {
                case 0:
                    return false;
                case -1:
                    tickable.tick();
                default:
                    remainingTicks--;
            }
            return true;
        }

    }

}
