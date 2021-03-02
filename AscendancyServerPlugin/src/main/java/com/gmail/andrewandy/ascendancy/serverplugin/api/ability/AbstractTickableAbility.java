package com.gmail.andrewandy.ascendancy.serverplugin.api.ability;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.util.game.Tickable;

import java.util.UUID;

/**
 * Represents an ability which may need to be ticked.
 */
public abstract class AbstractTickableAbility extends AbstractAbility implements Tickable {

    private final UUID uuid = UUID.randomUUID();

    public AbstractTickableAbility(
            final String name, final boolean isActive,
            final Challenger bound
    ) {
        super(name, isActive, bound);
    }

    @Override
    public UUID getUniqueID() {
        return uuid;
    }

}
