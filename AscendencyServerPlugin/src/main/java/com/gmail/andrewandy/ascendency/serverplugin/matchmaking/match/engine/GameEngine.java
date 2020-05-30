package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.engine;

import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface GameEngine {

    void start();

    void end();

    void resume();

    void rejoin(UUID player) throws IllegalArgumentException;

    default boolean isActiveKeyPressed() {
        return false;
    }

    Optional<? extends GamePlayer> getGamePlayerOf(UUID player);

    Collection<Player> getPlayersOfChallenger(Challenger challenger);

}
