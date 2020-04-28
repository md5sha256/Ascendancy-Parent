package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.event;

import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.Match;
import org.spongepowered.api.event.Cancellable;

public class MatchStartEvent extends MatchEvent implements Cancellable {

    private boolean cancel;

    public MatchStartEvent(Match match) {
        super(match);
    }

    public MatchStartEvent(Match match, String name, Object cause) {
        super(match, name, cause);
    }

    @Override public boolean isCancelled() {
        return cancel;
    }

    @Override public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
