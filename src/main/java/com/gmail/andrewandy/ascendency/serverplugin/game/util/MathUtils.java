package com.gmail.andrewandy.ascendency.serverplugin.game.util;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

public class MathUtils {

    public static Collection<Location<World>> createCircle(Location<World> centre, int radius) {
        if (radius < 0) {
            radius = -radius;
        }
        if (radius == 0) {
            throw new UnsupportedOperationException();
        }
        int x = centre.getBlockX();
        int y = centre.getBlockY();
        int z = centre.getBlockZ();
        //Eq x2 = r2 - z2 // r2 = x2 + z2 // z2 = r2 - x2
        int startX = x + radius;
        Collection<Location<World>> locations = new HashSet<>();
        int last = Integer.MAX_VALUE;
        for (int index = startX; last != startX; index++) {
            double zCoord = Math.pow(radius, 2) - Math.pow(z, 2);
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
    public static Collection<Location<World>> createSphere(Location<World> centre, int radius) {
        if (radius < 0) {
            radius = -radius;
        }
        if (radius == 0) {
            throw new UnsupportedOperationException();
        }
        int x = centre.getBlockX();
        int y = centre.getBlockY();
        int z = centre.getBlockZ();
        int lastX = Integer.MAX_VALUE;
        Collection<Location<World>> locations = new HashSet<>();
        for (int xCoord = x; lastX != x; xCoord++) {
            int lastJ = Integer.MAX_VALUE;
            double zCoord = Math.pow(radius, 2) - Math.pow(z, 2);
            for (int j = y; lastJ != y; j++) {
                double yCoord = radius - (zCoord
                    - z); //Basically taking the radius minus diff between centre and outer ring.
                Location<World> location =
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

    public static Predicate<Location<World>> isWithinSphere(Location<World> centre, int radius) {
        return (Location<World> location) -> calculateDistance(centre, location) < radius;
    }

    public static double calculateDistance(double x1, double y1, double z1, double x2, double y2,
        double z2) {
        return Math.sqrt(
            Math.pow(x1 - x2, 2) + Math.pow(z1 - z2, 2) + Math.pow(y1 - y2, 2)); //Find  3D distance
    }

    public static double calculateDistance(Location<World> primary, Location<World> secondary) {
        if (primary.getExtent() != secondary.getExtent()) {
            throw new UnsupportedOperationException(
                "Cannot calculate distances for different worlds!");
        }
        double x1 = primary.getX(), x2 = secondary.getX(), y1 = primary.getY(), y2 =
            secondary.getY(), z1 = primary.getZ(), z2 = secondary.getZ();
        return calculateDistance(x1, y1, z1, x2, y2, z2);
    }

}
