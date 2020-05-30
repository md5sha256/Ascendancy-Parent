package com.gmail.andrewandy.ascendency.serverplugin.util.keybind;

import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class ActiveKeyReleasedEvent extends AscendencyServerEvent {

    private final Player player;
    private final Cause cause;

    ActiveKeyReleasedEvent(final Player player) {
        this.player = player;
        this.cause = Cause.builder().named("Player", player).build();
    }

    public Player getPlayer() {
        return player;
    }

    @Override public Cause getCause() {
        return cause;
    }
}
