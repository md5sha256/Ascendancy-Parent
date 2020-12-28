package com.gmail.andrewandy.ascendancy.serverplugin.api.rune;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;

public interface PlayerSpecificRune extends EntityRune {

    void applyTo(Player player);

    void clearFrom(Player player);

    @Override
    default void applyTo(final Entity entity) {
        if (!canApplyTo(entity)) {
            throw new UnsupportedOperationException();
        }
        applyTo((Player) entity);
    }

    default void clearFrom(final Entity entity) {
        if (entity instanceof Player) {
            clearFrom((Player) entity);
        }
    }

    @Override
    default void clearFrom(final Object object) {
        if (!canApplyTo(object)) {
            return;
        }
        clearFrom((Player) object);
    }

    @Override
    default boolean canApplyTo(final Object object) {
        return object instanceof Player;
    }
}
