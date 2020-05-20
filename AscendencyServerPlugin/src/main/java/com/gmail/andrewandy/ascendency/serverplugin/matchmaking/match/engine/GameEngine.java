package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.engine;

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

}
