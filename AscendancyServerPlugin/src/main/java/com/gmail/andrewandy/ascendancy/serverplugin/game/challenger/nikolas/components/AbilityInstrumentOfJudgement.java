package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.nikolas.components;

import com.flowpowered.math.vector.Vector3d;
import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractCooldownAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.util.keybind.ActiveKeyPressedEvent;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.FishHook;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.FishingEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class AbilityInstrumentOfJudgement extends AbstractCooldownAbility {

    @AssistedInject
    AbilityInstrumentOfJudgement(@Assisted final Challenger challenger) {
        super("Instrument of Judgement", true, 15, TimeUnit.SECONDS, challenger);
    }

    private static void pullEntityToLocation(final Entity entity, Vector3d loc) {
        final Location<World> entityLoc = entity.getLocation();
        entityLoc.add(0, 0.5, 0);
        entity.setLocation(entityLoc);

        double g = -0.08;
        double distance = entityLoc.getPosition().distance(loc);
        double velocityX = (1.0 + 0.07 * distance) * (loc.getX() - entityLoc.getX()) / distance;
        double velocityY = (1.0 + 0.03 * distance) * (loc.getY() - entityLoc.getY()) / distance - 0.5 * g * distance;
        double velocityZ = (1.0 + 0.07 * distance) * (loc.getZ() - entityLoc.getZ()) / distance;

        Vector3d velocity = new Vector3d(velocityX, velocityY, velocityZ);
        entity.setVelocity(velocity);
    }

    @Listener(order = Order.LAST)
    public void onActiveKeyPress(final ActiveKeyPressedEvent event) {
        final Player player = event.getPlayer();
        if (!isRegistered(player.getUniqueId()) || isOnCooldown(player.getUniqueId())) {
            return;
        }
        // TODO check if the velocity is correct here
        player.launchProjectile(FishHook.class);
    }

    @Listener(order = Order.LAST)
    public void onGrapplingHookLand(final FishingEvent.HookEntity event) {
        final FishHook fishHook = event.getFishHook();
        final ProjectileSource source = fishHook.getShooter();
        if (!(source instanceof Player)) {
            return;
        }
        final Player player = (Player) source;
        // Check if player is registered
        if (!isRegistered(player.getUniqueId())) {
            return;
        }
        final Optional<Entity> optionalEntity = fishHook.getHookedEntity();
        final Vector3d target;

        if (optionalEntity.isPresent()) {
            // FIXME damage the hooked entity;
            target = event.getTargetEntity().getLocation().getPosition();
        } else {
            // Use the block the hook is in instead.
            target = fishHook.getLocation().getPosition().add(0, 0.5, 0);
        }
        final double distance = player.getLocation().getPosition().distanceSquared(target);
        if (distance < 30 * 30) {
            // If enemy, pull enemy to Nikolas
            if (optionalEntity.isPresent()) {
                pullEntityToLocation(optionalEntity.get(), player.getLocation().getPosition());
            } else {
                // Pull Nikolas to the block
                pullEntityToLocation(player, target);
            }
        }
        // Reset the cooldown
        resetCooldown(player.getUniqueId());
    }

}
