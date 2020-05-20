package com.gmail.andrewandy.ascendency.serverplugin.api.rune;

import org.spongepowered.api.entity.Entity;

public interface EntityRune extends Rune {

    void applyTo(Entity entity);

    void clearFrom(Entity entity);

    @Override
    default void clearFrom(Object object) {
        if (!canApplyTo(object)) {
            return;
        }
        clearFrom((Entity) object);
    }

    @Override
    default void applyTo(Object object) {
        if (!canApplyTo(object)) {
            throw new UnsupportedOperationException();
        }
        applyTo((Entity) object);
    }

    @Override
    default boolean canApplyTo(Object object) {
        return object instanceof Entity;
    }
}
