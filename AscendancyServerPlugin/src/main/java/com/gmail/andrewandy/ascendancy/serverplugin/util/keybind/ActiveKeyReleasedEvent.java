package com.gmail.andrewandy.ascendancy.serverplugin.util.keybind;

import com.gmail.andrewandy.ascendancy.serverplugin.api.event.AscendancyServerEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class ActiveKeyReleasedEvent extends AscendancyServerEvent {

    @NotNull
    private final Player player;
    @NotNull
    private final Cause cause;

    ActiveKeyReleasedEvent(@NotNull final Player player) {
        this.player = player;
        this.cause = Cause.builder().named("Player", player).build();
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    @NotNull
    public Cause getCause() {
        return cause;
    }

}
