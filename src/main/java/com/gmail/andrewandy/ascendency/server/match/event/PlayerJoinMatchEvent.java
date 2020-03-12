package com.gmail.andrewandy.ascendency.server.match.event;

import com.gmail.andrewandy.ascendency.server.match.Match;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import java.util.Objects;
import java.util.UUID;

@Cancelable
public class PlayerJoinMatchEvent extends MatchEvent {

    private UUID player;

    public PlayerJoinMatchEvent(UUID player, Match joined) {
        super(joined);
        this.player = Objects.requireNonNull(player);
    }

    public UUID getPlayer() {
        return player;
    }


}
