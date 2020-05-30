package com.gmail.andrewandy.ascendency.serverplugin.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
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


    public static class PlayerJumpEvent extends AbstractEvent implements Cancellable {

        private final MoveEntityEvent originalEvent;

        public PlayerJumpEvent(MoveEntityEvent original) {
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

        @Override public boolean isCancelled() {
            return originalEvent.isCancelled();
        }

        @Override public void setCancelled(final boolean cancel) {
            originalEvent.setCancelled(cancel);
        }

        @Override public Cause getCause() {
            return originalEvent.getCause();
        }
    }

    /**
     * Calls {@link PlayerJumpEvent}
     */
    @Listener(order = Order.FIRST)
    public void onPlayerMove(MoveEntityEvent event) {
        if (!(event.getTargetEntity() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getTargetEntity();
        Transform<World> from = event.getFromTransform();
        Transform<World> to = event.getToTransform();
        if (from.getLocation().getBlock().getType() == BlockTypes.AIR) {
            return;
        }
        if (to.getLocation().getBlock().getType() == BlockTypes.AIR && player.isOnGround()) {
            new PlayerJumpEvent(event).callEvent();
        }
    }
}
