package com.gmail.andrewandy.ascendancy.serverplugin.game.util;

import com.flowpowered.math.vector.Vector2i;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

public class MathUtils {

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

        final int xMax = x + radius + 1;
        final int xMin = x - radius - 1;
        final int zMax = z + radius + 1;
        final int zMin = z - radius - 1;
        final int radiusSquared = radius * radius;

        for (int i = xMin; i < xMax; i++) {
            for (int j = zMin; j < zMax; j++) {
                if (Vector2i.from(x, z).distanceSquared(i, j) <= radiusSquared) {
                    collection.add(new Location<>(world, i, y, j));
                }
            }
        }
        return collection;
    }

    public static boolean isWithinCircle(@NotNull final Location<World> centre, final int radius,
                                         @NotNull final Location<World> target) {
        if (centre.getExtent() != target.getExtent()) {
            throw new UnsupportedOperationException(
                    "Cannot calculate distances for different worlds!");
        }
        return centre.getPosition().distanceSquared(target.getPosition()) <= radius * radius;
    }


    @NotNull
    public static Predicate<Location<World>> isWithinSphere(@NotNull final Location<World> centre,
                                                            final int radius) {
        return (Location<World> location) -> location.getPosition().distanceSquared(centre.getPosition()) <= radius * radius;
    }

    public static <E extends Extent> boolean isWithinSphere(@NotNull final Location<E> centre,
                                                            final double radius,
                                                            @NotNull final Location<E> test) {

        return centre.getPosition().distanceSquared(test.getPosition()) <= radius * radius;
    }

}
