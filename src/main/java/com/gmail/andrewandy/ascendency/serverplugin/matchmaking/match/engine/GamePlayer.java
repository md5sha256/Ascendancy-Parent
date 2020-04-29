package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.engine;

import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.Rune;
import org.spongepowered.api.effect.potion.PotionEffect;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface GamePlayer {

    UUID getPlayerUUID();

    Collection<Rune> getRunes();

    Collection<PotionEffect> getStatusEffects();

    default Optional<PotionEffect> getStatusEffect(final String effectID) {
        return getStatusEffects().stream()
            .filter(((PotionEffect pe) -> pe.getType().getName().equalsIgnoreCase(effectID)))
            .findAny();
    }

    void addStatusEffect(PotionEffect effect);

    default void removeStatusEffect(final String effectID) {
        getStatusEffect(effectID).ifPresent(this::removeStatusEffect);
    }

    void removeStatusEffect(PotionEffect effect);

    Challenger getChallenger();

}
