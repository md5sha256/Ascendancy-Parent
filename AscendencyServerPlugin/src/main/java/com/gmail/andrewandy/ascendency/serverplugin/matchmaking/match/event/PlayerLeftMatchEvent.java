package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.event;

import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.Match;

import java.util.Objects;
import java.util.UUID;

public class PlayerLeftMatchEvent extends MatchEvent {

    private UUID player;

    public PlayerLeftMatchEvent(UUID player, Match match) {
        super(match);
        this.player = Objects.requireNonNull(player);
    }

    public PlayerLeftMatchEvent(Match match, UUID player) {
        super(match);
        this.player = player;
    }

    public PlayerLeftMatchEvent(Match match, UUID player, String name, Object cause) {
        super(match, name, cause);
        this.player = player;
    }

    public UUID getPlayer() {
        return player;
    }
}
