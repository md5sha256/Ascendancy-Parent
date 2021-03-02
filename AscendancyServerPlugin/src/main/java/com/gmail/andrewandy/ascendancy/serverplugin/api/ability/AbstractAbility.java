package com.gmail.andrewandy.ascendancy.serverplugin.api.ability;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public abstract class AbstractAbility implements Ability {

    protected final Challenger bound;
    private final String name;
    private final boolean isActive;
    protected Collection<UUID> registered = new HashSet<>();

    public AbstractAbility(final String name, final boolean isActive, final Challenger bound) {
        this.name = name;
        this.isActive = isActive;
        this.bound = bound;
    }

    @Override
    public Challenger getBoundChallenger() {
        return bound;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isPassive() {
        return !isActive;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void register(final UUID player) {
        this.registered.add(player);
    }

    @Override
    public void unregister(final UUID player) {
        this.registered.remove(player);
    }

    @Override
    public boolean isRegistered(final UUID player) {
        return registered.contains(player);
    }

    @Override
    public Collection<UUID> getRegistered() {
        return new HashSet<>(registered);
    }

}
