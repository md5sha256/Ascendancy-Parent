package com.gmail.andrewandy.ascendency.serverplugin.util.keybind;

import com.gmail.andrewandy.ascendency.serverplugin.api.event.AscendencyServerEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;

import java.util.Objects;

public class ActiveKeyPressedEvent extends AscendencyServerEvent implements Cancellable {

    @NotNull private final Player player;
    @NotNull private final Cause cause;
    private boolean cancel;

    ActiveKeyPressedEvent(@NotNull final Player player) {
        this.player = Objects.requireNonNull(player);
        this.cause = Cause.builder().named("Player", player).build();
    }

    @NotNull public Player getPlayer() {
        return player;
    }

    @Override public boolean isCancelled() {
        return cancel;
    }

    @Override public void setCancelled(final boolean cancel) {
        this.cancel = cancel;
    }

    @Override @NotNull public Cause getCause() {
        return cause;
    }
}
