package com.gmail.andrewandy.ascendancy.serverplugin.api.event;

import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.Team;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public abstract class GameEvent extends AscendancyServerEvent {

    private final Team playerTeam;
    private final Player player;
    private final Cause cause;


    public GameEvent(final Player player, final Team team) {
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
    public @NotNull Cause getCause() {
        return cause;
    }

}
