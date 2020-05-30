package com.gmail.andrewandy.ascendency.serverplugin.util.game;

import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import org.spongepowered.api.Sponge;

import java.util.Collection;
import java.util.HashSet;

public class TickHandler {

    private static final TickHandler instance = new TickHandler();
    private final Collection<TickData> toTick = new HashSet<>();

    private TickHandler() {
        Sponge.getScheduler().createTaskBuilder().execute(this::run).intervalTicks(1)
            .submit(AscendencyServerPlugin.getInstance());
    }

    public static TickHandler getInstance() {
        return instance;
    }

    public void submitTickable(final Tickable tickable) {
        removeTickable(tickable);
        submitTickable(tickable, 1);
    }

    public void removeTickable(final Tickable tickable) {
        toTick.removeIf((tickData) -> tickData.getTickable() == tickable);
    }

    public void submitTickable(final Tickable tickable, final int ticks) {
        removeTickable(tickable);
        toTick.add(new TickData(tickable, ticks));
    }

    public void run() {
        toTick.removeIf(TickData::failedTick);
    }

    private class TickData {

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
