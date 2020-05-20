package com.gmail.andrewandy.ascendency.serverplugin.game.util;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.HashSet;

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
     * @param centre The centre of the cuboid.
     * @param radius The radius.
     * @return Returns a Collection of locations which are within this cuboid.
     */
    public static Collection<Location<World>> createCube(Location<World> centre, int radius) {
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
                double yCoord = radius - (zCoord - z); //Basically taking the radius minus diff between centre and outer ring.
                locations.add(new Location<>(centre.getExtent(), xCoord, yCoord, zCoord));
                lastJ = j;
            }
            lastX = xCoord;
        }
        return locations;
    }

}
