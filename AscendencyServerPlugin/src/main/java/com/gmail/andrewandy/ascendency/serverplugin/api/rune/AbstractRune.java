package com.gmail.andrewandy.ascendency.serverplugin.api.rune;

import java.util.UUID;

public abstract class AbstractRune implements PlayerSpecificRune {

    private UUID uuid = UUID.randomUUID();

    @Override
    public UUID getUniqueID() {
        return uuid;
    }
}
