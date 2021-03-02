package com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.event;

import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.Match;
import org.spongepowered.api.event.Cancellable;

import java.util.Objects;
import java.util.UUID;

public class PlayerJoinMatchEvent extends MatchEvent implements Cancellable {

    private UUID player;
    private boolean cancel;

    public PlayerJoinMatchEvent(UUID player, Match joined) {
        super(joined);
        this.player = Objects.requireNonNull(player);
    }

    public UUID getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

}
