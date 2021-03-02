package com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.event;

import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.Match;
import org.spongepowered.api.event.cause.Cause;

public class MatchEndedEvent extends MatchEvent {

    public MatchEndedEvent(final Match match) {
        super(match);
    }

    public MatchEndedEvent(final Match match, final String name, final Object cause) {
        super(match, name, cause);
    }

    @Override
    public Cause getCause() {
        return null;
    }

}
