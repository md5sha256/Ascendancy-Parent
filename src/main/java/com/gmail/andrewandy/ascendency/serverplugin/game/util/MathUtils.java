package com.gmail.andrewandy.ascendency.serverplugin.game.util;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

public class MathUtils {

    public static Collection<Location<World>> createCircle(final Location<World> centre, int radius) {
        if (radius < 0) {
            radius = -radius;
        }
        if (radius == 0) {
            throw new UnsupportedOperationException();
        }
        final int x = centre.getBlockX();
        final int y = centre.getBlockY();
        final int z = centre.getBlockZ();
        //Eq x2 = r2 - z2 // r2 = x2 + z2 // z2 = r2 - x2
        final int startX = x + radius;
        final Collection<Location<World>> locations = new HashSet<>();
        int last = Integer.MAX_VALUE;
        for (int index = startX; last != startX; index++) {
            final double zCoord = Math.pow(radius, 2) - Math.pow(z, 2);
            locations.add(new Location<>(centre.getExtent(), index, y, zCoord));
            last = index;
        }
        return locations;
    }

    /**
     * Untested math! Should in theory, create a list of blocks in a cuboid.
     *
     * @param centre The centre of the cuboid.
     * @param radius The radius.
     * @return Returns a Collection of block locations which are within this cuboid.
     */
    public static Collection<Location<World>> createSphere(final Location<World> centre, int radius) {
        if (radius < 0) {
            radius = -radius;
        }
        if (radius == 0) {
            throw new UnsupportedOperationException();
        }
        final int x = centre.getBlockX();
        final int y = centre.getBlockY();
        final int z = centre.getBlockZ();
        int lastX = Integer.MAX_VALUE;
        final Collection<Location<World>> locations = new HashSet<>();
        for (int xCoord = x; lastX != x; xCoord++) {
            int lastJ = Integer.MAX_VALUE;
            final double zCoord = Math.pow(radius, 2) - Math.pow(z, 2);
            for (int j = y; lastJ != y; j++) {
                final double yCoord = radius - (zCoord
                    - z); //Basically taking the radius minus diff between centre and outer ring.
                final Location<World> location =
                    new Location<>(centre.getExtent(), Math.round(xCoord), Math.round(yCoord),
                        Math.round(zCoord));
                locations.add(
                    new Location<>(centre.getExtent(), location.getBlockX(), location.getBlockY(),
                        location.getBlockZ()));
                lastJ = j;
            }
            lastX = xCoord;
        }
        return locations;
    }

    public static Predicate<Location<World>> isWithinSphere(final Location<World> centre, final int radius) {
        return (Location<World> location) -> calculateDistance(centre, location) < radius;
    }

    public static double calculateDistance(final double x1, final double y1, final double z1, final double x2, final double y2,
        final double z2) {
        return Math.sqrt(
            Math.pow(x1 - x2, 2) + Math.pow(z1 - z2, 2) + Math.pow(y1 - y2, 2)); //Find  3D distance
    }

    public static double calculateDistance(final Location<World> primary, final Location<World> secondary) {
        if (primary.getExtent() != secondary.getExtent()) {
            throw new UnsupportedOperationException(
                "Cannot calculate distances for different worlds!");
        }
        final double x1 = primary.getX(), x2 = secondary.getX(), y1 = primary.getY(), y2 =
            secondary.getY(), z1 = primary.getZ(), z2 = secondary.getZ();
        return calculateDistance(x1, y1, z1, x2, y2, z2);
    }

}
