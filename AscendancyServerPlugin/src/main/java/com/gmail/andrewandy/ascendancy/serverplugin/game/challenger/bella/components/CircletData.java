package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.components;

import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.gmail.andrewandy.ascendancy.serverplugin.game.util.MathUtils;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Class used to represent data regarding a circlet being cast.
 */
public class CircletData {

    private static final int DEFAULT_RADIUS = 3;

    private final int radius;
    private final AscendancyServerPlugin plugin;
    private UUID caster;
    private long tickCount = 0;
    private Location<World> ringCenter;
    private Collection<Location<World>> ringBlocks;

    @AssistedInject
    CircletData(@Assisted final UUID caster, @Assisted final int radius,
                final AscendancyServerPlugin plugin) {
        this.plugin = plugin;
        this.caster = caster;
        if (radius < 1) {
            throw new IllegalArgumentException("Radius must be greater than 1");
        }
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }

    public UUID getCaster() {
        return caster;
    }

    public void setCaster(@NotNull final UUID caster) {
        this.caster = caster;
    }

    public void incrementTick() {
        this.tickCount++;
    }

    public Collection<Location<World>> getRingBlocks() {
        return ringBlocks == null ? new HashSet<>() : new HashSet<>(ringBlocks);
    }

    public void setRingBlocks(final Collection<Location<World>> ringBlocks) {
        this.ringBlocks = ringBlocks;
    }

    public Location<World> getRingCenter() {
        return ringCenter;
    }

    public void setRingCenter(final Location<World> ringCenter) {
        this.ringCenter = ringCenter;
    }

    public long getTickCount() {
        return tickCount;
    }

    public Predicate<Location<World>> generateCircleTest() {
        if (ringCenter != null) {
            return MathUtils.isWithinSphere(ringCenter, DEFAULT_RADIUS);
        }
        return (unused) -> false;
    }

    public void reset() {
        this.tickCount = 0L;
        this.ringCenter = null;
        final Cause cause = Cause.builder().named("Bella", plugin).build();
        ringBlocks.forEach(location -> location.setBlockType(BlockTypes.AIR, cause));
        this.ringBlocks = null;
    }
}
