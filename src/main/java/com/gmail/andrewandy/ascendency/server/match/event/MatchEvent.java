package com.gmail.andrewandy.ascendency.server.match.event;

import com.gmail.andrewandy.ascendency.server.AscendencyServerEvent;
import com.gmail.andrewandy.ascendency.server.match.Match;

import java.util.Objects;

/**
 * Represents a event relating to a {@link Match}
 */
public abstract class MatchEvent extends AscendencyServerEvent {

    private Match match;

    public MatchEvent(Match match) {
        super();
        this.match = Objects.requireNonNull(match);
    }

    public Match getMatch() {
        return match;
    }
}
