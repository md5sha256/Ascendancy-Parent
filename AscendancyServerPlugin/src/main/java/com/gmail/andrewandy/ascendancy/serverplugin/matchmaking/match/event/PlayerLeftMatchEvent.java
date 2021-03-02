package com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.event;

import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.Match;

import java.util.Objects;
import java.util.UUID;

public class PlayerLeftMatchEvent extends MatchEvent {

    private final UUID player;

    public PlayerLeftMatchEvent(final UUID player, final Match match) {
        super(match);
        this.player = Objects.requireNonNull(player);
    }

    public PlayerLeftMatchEvent(final Match match, final UUID player) {
        super(match);
        this.player = player;
    }

    public PlayerLeftMatchEvent(
            final Match match, final UUID player, final String name,
            final Object cause
    ) {
        super(match, name, cause);
        this.player = player;
    }

    public UUID getPlayer() {
        return player;
    }

}
