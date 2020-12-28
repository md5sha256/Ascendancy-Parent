package com.gmail.andrewandy.ascendancy.serverplugin.game.util;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

public class MathUtils {

    /**
     * Create a circle with a radius of the provided radius.
     *
     * @param centre The centre of the circle.
     * @param radius The base radius of the circle.
     * @return Returns a collection of blocks within the circle.
     */
    @NotNull
    public static Collection<Location<World>> createCircle(
            @NotNull final Location<World> centre, int radius) {
        if (radius == 0) {
            throw new UnsupportedOperationException();
        }
        if (radius < 0) {
            radius = -radius;
        }
        final int x = centre.getBlockX();
        final int y = centre.getBlockY();
        final int z = centre.getBlockZ();
        //Eq x2 = r2 - z2 // r2 = x2 + z2 // z2 = r2 - x2
        final int xMax = x + radius, xMin = x - radius, zMax = z + radius, zMin = z - radius;
        final World world = centre.getExtent();
        final Collection<Location<World>> locations = new HashSet<>();
        for (int xCoord = xMin; xCoord < xMax; xCoord++) {
            for (int zCoord = zMin; zCoord < zMax; zCoord++) {
                if (calculateDistance2D(xCoord, zCoord, x, z) <= radius) {
                    locations.add(new Location<>(world, xCoord, y, zCoord));
                }
            }
        }
        return locations;
    }

    /**
     * Create a circle with a radius of the base radius + 0.5.
     *
     * @param centre The centre of the circle.
     * @param radius The base radius of the circle.
     * @return Returns a collection of blocks within the circle.
     */
    @NotNull
    public static Collection<Location<World>> createCircleWithCentre(
            @NotNull final Location<World> centre, int radius) {
        if (radius == 0) {
            throw new UnsupportedOperationException();
        }
        if (radius < 0) {
            radius = -radius;
        }
        final int x, y, z;
        x = centre.getBlockX();
        y = centre.getBlockY();
        z = centre.getBlockZ();
        final World world = centre.getExtent();
        final Collection<Location<World>> collection = new HashSet<>();
        final int xMax = x + radius + 1, xMin = x - radius - 1, zMax = z + radius + 1, zMin =
                z - radius - 1;
        for (int i = xMin; i < xMax; i++) {
            for (int j = zMin; j < zMax; j++) {
                if (Math.abs(calculateDistance2D(x, i, z, j)) <= radius) {
                    collection.add(new Location<>(world, i, y, j));
                }
            }
        }
        return collection;
    }

    public static boolean isWithinCircle(@NotNull final Location<World> centre, final int radius,
                                         @NotNull final Location<World> test) {
        if (centre.getExtent() != test.getExtent()) {
            throw new UnsupportedOperationException(
                    "Cannot calculate distances for different worlds!");
        }
        return calculateDistance2D(centre, test) <= radius;
    }

    /**
     * Create a sphere with a radius of the base radius + 0.5.
     *
     * @param centre The centre of the cuboid.
     * @param radius The radius.
     * @return Returns a Collection of block locations which are within this cuboid.
     */
    @NotNull
    public static Collection<Location<World>> createSphereWithCentre(
            @NotNull final Location<World> centre, int radius) {
        if (radius < 0) {
            radius = -radius;
        }
        if (radius == 0) {
            throw new UnsupportedOperationException();
        }
        final int x = centre.getBlockX();
        final int y = centre.getBlockY();
        final int z = centre.getBlockZ();
        final World world = centre.getExtent();
        final Collection<Location<World>> collection = new HashSet<>();
        final int xMax = x + radius + 1, xMin = x - radius - 1, yMax = y + radius + 1, yMin =
                y - radius - 1, zMax = z + radius + 1, zMin = z - radius - 1;
        for (int xCoord = xMin; xCoord < xMax; xCoord++) {
            for (int zCoord = zMin; zCoord < zMax; zCoord++) {
                for (int yCoord = yMin; yCoord < yMax; yCoord++) {
                    if (Math.abs(calculateDistance3D(xCoord, yCoord, zCoord, x, y, z)) <= radius) {
                        collection.add(new Location<>(world, xCoord, yCoord, zCoord));
                    }
                }
            }
        }
        return collection;
    }

    /**
     * Creates a sphere with the provided radius.
     *
     * @param centre The centre of the sphere.
     * @param radius The base radius of this sphere.
     * @return A collection of blocks within this sphere.
     */
    @NotNull
    public static Collection<Location<World>> createSphereWith(
            @NotNull final Location<World> centre, int radius) {
        if (radius < 0) {
            radius = -radius;
        }
        if (radius == 0) {
            throw new UnsupportedOperationException();
        }
        final int x = centre.getBlockX();
        final int y = centre.getBlockY();
        final int z = centre.getBlockZ();
        final World world = centre.getExtent();
        final Collection<Location<World>> collection = new HashSet<>();
        final int xMax = x + radius, xMin = x - radius, yMax = y + radius, yMin = y - radius, zMax =
                z + radius, zMin = z - radius;
        for (int xCoord = xMin; xCoord < xMax; xCoord++) {
            for (int zCoord = zMin; zCoord < zMax; zCoord++) {
                for (int yCoord = yMin; yCoord < yMax; yCoord++) {
                    if (Math.abs(calculateDistance3D(xCoord, yCoord, zCoord, x, y, z)) <= radius) {
                        collection.add(new Location<>(world, xCoord, yCoord, zCoord));
                    }
                }
            }
        }
        return collection;
    }

    @NotNull
    public static Predicate<Location<World>> isWithinSphere(@NotNull final Location<World> centre,
                                                            final int radius) {
        return (Location<World> location) -> calculateDistance3D(centre, location) <= radius;
    }

    public static <E extends Extent> boolean isWithinSphere(@NotNull final Location<E> centre,
                                                            final double radius,
                                                            @NotNull final Location<E> test) {
        return Math.abs(calculateDistance3D(centre, test)) <= radius;
    }

    public static double calculateDistance2D(final double x1, final double z1, final double x2,
                                             final double z2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (z1 - z2) * (z1 - z2));
    }

    public static <E extends Extent> double calculateDistance2D(@NotNull final Location<E> primary,
                                                                final Location<E> secondary) {
        if (primary.getExtent() != secondary.getExtent()) {
            throw new UnsupportedOperationException(
                    "Cannot calculate distances for different worlds!");
        }
        return calculateDistance2D(primary.getX(), secondary.getX(), primary.getZ(),
                secondary.getZ());
    }

    public static double calculateDistance3D(final double x1, final double y1, final double z1,
                                             final double x2, final double y2, final double z2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (z1 - z2) * (z1 - z2) + (y1 - y2) * (y1
                - y2)); //Find  3D distance
    }

    public static <E extends Extent> double calculateDistance3D(@NotNull final Location<E> primary,
                                                                final Location<E> secondary) {
        if (primary.getExtent() != secondary.getExtent()) {
            throw new UnsupportedOperationException(
                    "Cannot calculate distances for different worlds!");
        }
        final double x1 = primary.getX(), x2 = secondary.getX(), y1 = primary.getY(), y2 =
                secondary.getY(), z1 = primary.getZ(), z2 = secondary.getZ();
        return calculateDistance3D(x1, y1, z1, x2, y2, z2);
    }

}
