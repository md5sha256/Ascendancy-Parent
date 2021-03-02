package com.gmail.andrewandy.ascendancy.serverplugin.util.game;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.engine.GameEngine;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.engine.GamePlayer;
import org.spongepowered.api.entity.living.player.Player;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class DebugMatchEngine implements GameEngine {

    private final WeakReference<DebugMatch> matchReference;

    DebugMatchEngine(DebugMatch debugMatch) {
        this.matchReference = new WeakReference<>(debugMatch);
    }

    public Optional<DebugMatch> getMatchReference() {
        return Optional.ofNullable(matchReference.get());
    }

    @Override
    public void start() {

    }

    @Override
    public void end() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void rejoin(final UUID player) throws IllegalArgumentException {

    }

    @Override
    public Optional<? extends GamePlayer> getGamePlayerOf(final UUID player) {
        return Optional.empty();
    }

    @Override
    public Collection<Player> getPlayersOfChallenger(final Challenger challenger) {
        return null;
    }

}
