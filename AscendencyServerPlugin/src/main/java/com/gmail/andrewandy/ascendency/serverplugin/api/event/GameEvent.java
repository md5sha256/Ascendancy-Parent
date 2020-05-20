package com.gmail.andrewandy.ascendency.serverplugin.api.event;

import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.AscendencyServerEvent;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Team;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public abstract class GameEvent extends AscendencyServerEvent {

    private final Team playerTeam;
    private final Player player;
    private final Cause cause;


    public GameEvent(Player player, Team team) {
        this.player = player;
        this.playerTeam = team;
        this.cause = Cause.builder().named("Player", player).build();
    }

    public Player getPlayer() {
        return player;
    }

    public Team getPlayerTeam() {
        return playerTeam;
    }

    @Override
    public Cause getCause() {
        return cause;
    }
}
