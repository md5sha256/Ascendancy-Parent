package com.gmail.andrewandy.ascendancy.serverplugin.game.util;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * Class used to store mark data
 */
public class LocationMark {

    private Location<World> primaryMark, secondaryMark;
    private long primaryTick, secondaryTick;

    public Location<World> getPrimaryMark() {
        return primaryMark;
    }

    public void setPrimaryMark(final Location<World> primaryMark) {
        this.primaryMark = primaryMark;
    }

    public Location<World> getSecondaryMark() {
        return secondaryMark;
    }

    public void setSecondaryMark(final Location<World> secondaryMark) {
        this.secondaryMark = secondaryMark;
    }

    public void clear() {
        resetPrimaryTick();
        resetSecondaryTick();
        primaryMark = null;
        secondaryMark = null;
    }

    public void resetPrimaryTick() {
        this.primaryTick = 0;
    }

    public void resetSecondaryTick() {
        this.secondaryTick = 0;
    }

    public void incrementPrimary() {
        primaryTick++;
    }

    public void incrementSecondary() {
        secondaryTick++;
    }

    public void incrementAll() {
        incrementPrimary();
        incrementSecondary();
    }

    public long getPrimaryTick() {
        return primaryTick;
    }

    public long getSecondaryTick() {
        return secondaryTick;
    }

}
