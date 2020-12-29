package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.nikolas.components;

import com.flowpowered.math.vector.Vector3d;
import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractCooldownAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.util.keybind.ActiveKeyPressedEvent;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.FishingEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.concurrent.TimeUnit;

public class AbilityInstrumentOfJudgement extends AbstractCooldownAbility {

    @AssistedInject
    AbilityInstrumentOfJudgement(@Assisted final Challenger challenger) {
        super("Instrument of Judgement", true, 15, TimeUnit.SECONDS, challenger);
    }

    private static void pullEntityToLocation(final Entity entity, Location<World> loc) {
        final Location<World> entityLoc = entity.getLocation();
        entityLoc.add(0, 0.5, 0);
        entity.setLocation(entityLoc);

        double g = -0.08;
        double distance = entityLoc.getPosition().distance(loc.getPosition());
        double velocityX = (1.0 + 0.07 * distance) * (loc.getX() - entityLoc.getX()) / distance;
        double velocityY = (1.0 + 0.03 * distance) * (loc.getY() - entityLoc.getY()) / distance - 0.5 * g * distance;
        double velocityZ = (1.0 + 0.07 * distance) * (loc.getZ() - entityLoc.getZ()) / distance;

        Vector3d velocity = new Vector3d(velocityX, velocityY, velocityZ);
        entity.setVelocity(velocity);
    }

    @Listener(order = Order.LAST)
    public void onActiveKeyPress(final ActiveKeyPressedEvent event) {
        final Player player = event.getPlayer();
        if (!isRegistered(player.getUniqueId())) {
            return;
        }
        // Fishing rod code

        // End
    }

    //FIXME use correct fishing event
    @Listener(order = Order.LAST)
    public void onGrapplingHookLand(final FishingEvent.HookEntity event) {
        final Entity hooked = event.getTargetEntity();
        final Location<World> hookLandedPosition = hooked.getLocation();
        final Location<World> location = null;
        final double distance = hookLandedPosition.getPosition().distance(location.getPosition());
        if (distance > 30) {
            return;
        }
        pullEntityToLocation(null, location);
        // TODO damage the player
        resetCooldown(null);
    }

}
