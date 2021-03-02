package com.gmail.andrewandy.ascendancy.serverplugin.api.rune;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class AbstractRune implements PlayerSpecificRune {

    protected final Challenger bound;
    private final UUID uuid = UUID.randomUUID();

    public AbstractRune(@NotNull final Challenger bound) {
        this.bound = bound;
    }

    @Override
    public Challenger getBoundChallenger() {
        return bound;
    }

    @Override
    public UUID getUniqueID() {
        return uuid;
    }

}
