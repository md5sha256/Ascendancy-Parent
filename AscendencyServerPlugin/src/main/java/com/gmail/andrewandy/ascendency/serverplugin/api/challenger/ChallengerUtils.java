package com.gmail.andrewandy.ascendency.serverplugin.api.challenger;

import com.gmail.andrewandy.ascendency.serverplugin.util.Common;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ChallengerUtils {

    /**
     * Increment the tick count of each individual element in a map.
     *
     * @param removeAfter The time in which the entry should be removed.
     * @param unit        The unit of time.
     * @param onRemove    An action to be done when an element is removed, can be null.
     * @return Returns a predicate to be used in {@link java.util.Collection#removeIf(Predicate)}
     */
    public static Predicate<Map.Entry<UUID, Long>> mapTickPredicate(long removeAfter, TimeUnit unit, Consumer<UUID> onRemove) {
        return mapTickPredicate(Common.toTicks(removeAfter, unit), onRemove);
    }

    /**
     * Increment the tick count of each individual element in a map.
     * @param removeAfter The tick count in which the entry should be removed.
     * @param onRemove An action to be done when an element is removed, can be null/
     * @return Returns a predicate to be used in {@link java.util.Collection#removeIf(Predicate)}
     */
    public static Predicate<Map.Entry<UUID, Long>> mapTickPredicate(long removeAfter, Consumer<UUID> onRemove) {
        return (Map.Entry<UUID, Long> entry) -> {
            entry.setValue(entry.getValue() + 1); //Increment tick count
            if (entry.getValue() >= removeAfter) {
                if (onRemove != null) onRemove.accept(entry.getKey());
                return true;
            }
            return false;
        }; //Clear if greater than the number of ticks in x seconds.
    }
}
