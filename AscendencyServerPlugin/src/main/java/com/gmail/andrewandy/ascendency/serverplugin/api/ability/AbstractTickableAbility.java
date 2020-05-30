package com.gmail.andrewandy.ascendency.serverplugin.api.ability;

import com.gmail.andrewandy.ascendency.serverplugin.util.game.Tickable;

import java.util.UUID;

/**
 * Represents an ability which may need to be ticked.
 */
public abstract class AbstractTickableAbility extends AbstractAbility implements Tickable {

    private final UUID uuid = UUID.randomUUID();

    public AbstractTickableAbility(final String name, final boolean isActive) {
        super(name, isActive);
    }

    @Override public UUID getUniqueID() {
        return uuid;
    }

}
