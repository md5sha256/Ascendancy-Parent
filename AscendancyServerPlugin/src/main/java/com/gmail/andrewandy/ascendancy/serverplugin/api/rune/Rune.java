package com.gmail.andrewandy.ascendancy.serverplugin.api.rune;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.util.game.Tickable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.DataSerializable;

/**
 * Represents an Ascnedency "Rune", can be applied to any object.
 */
public interface Rune extends DataSerializable, Tickable {

    Challenger getBoundChallenger();

    /**
     * Get the name of this rune.
     *
     * @return The name of the rune.
     */
    @NotNull String getName();

    /**
     * Apply this rune to the given object.
     *
     * @param object The object to apply to.
     * @throws IllegalArgumentException thrown if {@link #canApplyTo(Object)} returns false.
     */
    void applyTo(@NotNull Object object) throws IllegalArgumentException;

    /**
     * Clear this rune's effects from the target object.
     *
     * @param object The target object.
     */
    void clearFrom(@NotNull Object object);

    /**
     * Checks whether a given object can have this rune applied to it.
     *
     * @param object The object to apply to.
     * @return Returns whether or not this object can have this rune applied to it.
     */
    boolean canApplyTo(@NotNull Object object);
}
