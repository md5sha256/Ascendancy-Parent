package com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.event;

import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.Match;
import org.spongepowered.api.event.Cancellable;

public class MatchStartEvent extends MatchEvent implements Cancellable {

    private boolean cancel;

    public MatchStartEvent(final Match match) {
        super(match);
    }

    public MatchStartEvent(final Match match, final String name, final Object cause) {
        super(match, name, cause);
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        this.cancel = cancel;
    }
}
