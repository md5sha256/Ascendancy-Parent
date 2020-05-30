package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.draftpick;

import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.Rune;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.engine.GamePlayer;
import org.spongepowered.api.effect.potion.PotionEffect;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a player in an {@link DraftPickMatch}
 * This object holds data about the player which cannot
 * be directly attributed onto the player object for the {@link DraftPickMatchEngine}
 * to interpret and run the game accordingly.
 */
public class AscendencyPlayer implements GamePlayer {

    int relativeID;
    Collection<Rune> runes = new HashSet<>();
    Collection<PotionEffect> statusEffects = new HashSet<>();
    Challenger challenger;
    private final UUID player;

    AscendencyPlayer(final UUID player, final int relativeID) {
        this.player = player;
        this.relativeID = relativeID;
    }

    @Override public Collection<Rune> getRunes() {
        return runes;
    }

    @Override public Collection<PotionEffect> getStatusEffects() {
        return statusEffects;
    }

    @Override public void addStatusEffect(final PotionEffect effect) {
        removeStatusEffect(effect);
        statusEffects.add(effect);
    }

    @Override public void removeStatusEffect(final PotionEffect effect) {
        statusEffects.remove(effect);
    }

    /**
     * Get the champion the player has selected.
     *
     * @return Returns the Champion object which the player has selected.
     */
    public Challenger getChallenger() {
        return challenger;
    }

    @Override public UUID getPlayerUUID() {
        return player;
    }

    public boolean uuidMatches(final UUID other) {
        return other.equals(player);
    }

    @Override public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final AscendencyPlayer that = (AscendencyPlayer) o;

        if (relativeID != that.relativeID)
            return false;
        if (!Objects.equals(runes, that.runes))
            return false;
        if (!Objects.equals(challenger, that.challenger))
            return false;
        return Objects.equals(player, that.player);
    }

    @Override public int hashCode() {
        int result = relativeID;
        result = 31 * result + (runes != null ? runes.hashCode() : 0);
        result = 31 * result + (challenger != null ? challenger.hashCode() : 0);
        result = 31 * result + (player != null ? player.hashCode() : 0);
        return result;
    }
}
