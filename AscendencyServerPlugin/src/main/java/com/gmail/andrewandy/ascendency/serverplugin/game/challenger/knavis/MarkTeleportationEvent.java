package com.gmail.andrewandy.ascendency.serverplugin.game.challenger.knavis;

import com.gmail.andrewandy.ascendency.serverplugin.api.event.AscendencyServerEvent;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class MarkTeleportationEvent extends AscendencyServerEvent implements Cancellable {

    @NotNull private final Player player;
    @NotNull private final Cause cause;
    @NotNull private Location<World> location;
    private boolean cancel;

    @AssistedInject MarkTeleportationEvent(@Assisted final Player player, @Assisted final Location<World> toTeleport, final Knavis knavis) {
        this.player = player;
        this.location = toTeleport;
        this.cause = Cause.builder().named("Knavis", knavis).build();
    }

    public Location<World> getTargetLocation() {
        return location;
    }

    public void setTargetLocation(final Location<World> location) {
        this.location = location;
    }

    @NotNull public Player getPlayer() {
        return player;
    }

    @Override @NotNull public Cause getCause() {
        return cause;
    }

    @Override public boolean isCancelled() {
        return cancel;
    }

    @Override public void setCancelled(final boolean cancel) {
        this.cancel = cancel;
    }
}
