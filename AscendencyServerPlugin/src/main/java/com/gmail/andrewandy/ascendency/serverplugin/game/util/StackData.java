package com.gmail.andrewandy.ascendency.serverplugin.game.util;

import com.gmail.andrewandy.ascendency.serverplugin.util.Common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class StackData {

        private final Map<UUID, Long> stackTime = new HashMap<>();

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
            final Long val = stackTime.get(player);
            return val == null ? 0L : val;
        }


        public void tick() {
            stackTime.entrySet().forEach((entry -> entry.setValue(entry.getValue() + 1)));
        }

        public int calculateStacks() {
            int stacks = 0;
            final long ticks = Common.toTicks(1, TimeUnit.SECONDS);
            for (final Map.Entry<UUID, Long> entry : stackTime.entrySet()) {
                final int seconds = (int) Math.floor(entry.getValue() / (double) ticks);
                stacks += seconds;
                if (stacks == 2) {
                    break;
                }
            }
            return stacks;
        }


    }
