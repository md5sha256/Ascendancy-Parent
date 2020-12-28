package com.gmail.andrewandy.ascendancy.serverplugin.game.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class StackData {

    private final Map<UUID, Long> stackTime = new HashMap<>();
    private final long ticksPerStack;

    public StackData(long duration, TimeUnit timeUnit) {
        if (timeUnit == TimeUnit.NANOSECONDS || timeUnit == TimeUnit.MICROSECONDS) {
            throw new UnsupportedOperationException("Highest precision supported is milliseconds!");
        }
        this.ticksPerStack = TimeUnit.MILLISECONDS.convert(duration, timeUnit);
        if (this.ticksPerStack < 1) {
            throw new IllegalArgumentException("Duration must be greater than 0!");
        }
    }

    public void addPlayer(final UUID player) {
        if (stackTime.containsKey(player)) {
            return;
        }
        stackTime.put(player, 0L);
    }

    public void removePlayer(final UUID player) {
        stackTime.remove(player);
    }

    public long getTickCount(final UUID player) {
        return stackTime.getOrDefault(player, 0L);
    }


    public void tick() {
        stackTime.entrySet().forEach((entry -> entry.setValue(entry.getValue() + 1)));
    }

    public int calculateStacks() {
        int stacks = 0;
        for (final Map.Entry<UUID, Long> entry : stackTime.entrySet()) {
            final int seconds = (int) Math.floor(entry.getValue() / (double) ticksPerStack);
            stacks += seconds;
            if (stacks == 2) {
                break;
            }
        }
        return stacks;
    }

}
