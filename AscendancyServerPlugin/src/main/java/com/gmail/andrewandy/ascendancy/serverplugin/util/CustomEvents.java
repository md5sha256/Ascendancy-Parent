package com.gmail.andrewandy.ascendancy.serverplugin.util;

import com.google.common.annotations.Beta;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.world.World;

import java.util.Objects;

public enum CustomEvents {

    INSTANCE;


    /**
     * Calls {@link PlayerJumpEvent}
     */
    @Listener(order = Order.FIRST)
    public void onPlayerMove(final MoveEntityEvent event) {
        if (!(event.getTargetEntity() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getTargetEntity();
        final Transform<World> from = event.getFromTransform();
        final Transform<World> to = event.getToTransform();
        if (from.getLocation().getBlock().getType() == BlockTypes.AIR) {
            return;
        }
        if (to.getLocation().getBlock().getType() == BlockTypes.AIR && player.isOnGround()) {
            new PlayerJumpEvent(event).callEvent();
        }
    }


    /**
     * Represents a jump event. This class is untested and not production ready.
     */
    @Beta
    public static class PlayerJumpEvent extends AbstractEvent implements Cancellable {

        private final MoveEntityEvent originalEvent;

        public PlayerJumpEvent(final MoveEntityEvent original) {
            if (!(Objects.requireNonNull(original) instanceof Player)) {
                throw new IllegalArgumentException("Entity must be a player!");
            }
            this.originalEvent = original;
        }

        public Player getPlayer() {
            return (Player) originalEvent.getTargetEntity();
        }

        public boolean callEvent() {
            return Sponge.getEventManager().post(this);
        }

        @Override
        public boolean isCancelled() {
            return originalEvent.isCancelled();
        }

        @Override
        public void setCancelled(final boolean cancel) {
            originalEvent.setCancelled(cancel);
        }

        @Override
        @NotNull
        public Cause getCause() {
            return originalEvent.getCause();
        }

    }
}
