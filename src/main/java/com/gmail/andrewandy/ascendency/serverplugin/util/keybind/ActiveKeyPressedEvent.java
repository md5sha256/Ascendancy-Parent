package com.gmail.andrewandy.ascendency.serverplugin.util.keybind;

import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;

import java.util.Objects;

public class ActiveKeyPressedEvent extends AscendencyServerEvent implements Cancellable {

    private final Player player;
    private boolean cancel;
    private final Cause cause;

    ActiveKeyPressedEvent(final Player player) {
        this.player = Objects.requireNonNull(player);
        this.cause = Cause.builder().named("Player", player).build();
    }

    public Player getPlayer() {
        return player;
    }

    @Override public boolean isCancelled() {
        return cancel;
    }

    @Override public void setCancelled(final boolean cancel) {
        this.cancel = cancel;
    }

    @Override public Cause getCause() {
        return cause;
    }
}
