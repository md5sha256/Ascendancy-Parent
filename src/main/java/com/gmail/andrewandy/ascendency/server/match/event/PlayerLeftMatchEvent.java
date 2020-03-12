package com.gmail.andrewandy.ascendency.server.match.event;

import com.gmail.andrewandy.ascendency.server.match.Match;

import java.util.Objects;
import java.util.UUID;

public class PlayerLeftMatchEvent extends MatchEvent {

    private UUID player;

    public PlayerLeftMatchEvent(UUID player, Match match) {
        super(match);
        this.player = Objects.requireNonNull(player);
    }

    public UUID getPlayer() {
        return player;
    }
}
