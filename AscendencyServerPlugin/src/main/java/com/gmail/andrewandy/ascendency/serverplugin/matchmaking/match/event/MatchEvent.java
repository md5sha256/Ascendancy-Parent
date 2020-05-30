package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.event;

import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerEvent;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.Match;
import org.spongepowered.api.event.cause.Cause;

import java.util.Objects;

/**
 * Represents a event relating to a {@link Match}
 */
public abstract class MatchEvent extends AscendencyServerEvent {

    private final Match match;
    private Cause cause;

    public MatchEvent(final Match match) {
        this.match = Objects.requireNonNull(match);
        cause = Cause.builder().named("match", match).build();
    }

    public MatchEvent(final Match match, final String name, final Object cause) {
        this(match);
        this.cause = Cause.builder().named(name, cause).build();
    }

    public Match getMatch() {
        return match;
    }

    @Override public Cause getCause() {
        return cause;
    }
}
