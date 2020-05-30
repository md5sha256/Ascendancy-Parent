package com.gmail.andrewandy.ascendency.serverplugin.api.ability;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public abstract class AbstractAbility implements Ability {
    private final String name;
    private final boolean isActive;
    protected Collection<UUID> registered = new HashSet<>();

    public AbstractAbility(final String name, final boolean isActive) {
        this.name = name;
        this.isActive = isActive;
    }

    @Override public Collection<UUID> getRegistered() {
        return new HashSet<>(registered);
    }

    @Override public void register(final UUID player) {
        this.registered.add(player);
    }

    @Override public void unregister(final UUID player) {
        this.registered.remove(player);
    }

    @Override public boolean isRegistered(final UUID player) {
        return registered.contains(player);
    }

    @Override public boolean isPassive() {
        return !isActive;
    }

    @Override public boolean isActive() {
        return isActive;
    }

    @Override public String getName() {
        return name;
    }
}
