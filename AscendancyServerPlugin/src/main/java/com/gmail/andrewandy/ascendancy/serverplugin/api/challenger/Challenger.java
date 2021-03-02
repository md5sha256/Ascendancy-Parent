package com.gmail.andrewandy.ascendancy.serverplugin.api.challenger;

import com.gmail.andrewandy.ascendancy.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.PlayerSpecificRune;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a champion which players can select.
 */
public interface Challenger {

    /**
     * Get the name of this challenger.
     *
     * @return The name of the challenger.
     */
    @NotNull String getName();

    /**
     * Get this Challenger's runes
     *
     * @return Returns an array of this challenger's runes
     */
    @NotNull PlayerSpecificRune[] getRunes();

    /**
     * Get the lore of this challenger.
     *
     * @return Returns the lore.
     */
    @NotNull List<String> getLore();

    /**
     * Convert this champion into a serialisable data object.
     *
     * @return
     */
    @NotNull IChallengerData toData();

    /**
     * Get this Challenger's abilities
     *
     * @return Returns an array of this challenger's abilities
     */
    @NotNull Ability[] getAbilities();

    /**
     * Calculate the damage which should be dealt based on incoming damage.
     *
     * @param baseDamage The magnitude of the incoming damage.
     * @return The modified damage based on abilities etc.
     */
    default double calculateDamage(final double baseDamage) {
        return baseDamage;
    }

}
