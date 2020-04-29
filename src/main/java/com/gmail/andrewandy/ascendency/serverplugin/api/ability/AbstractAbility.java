package com.gmail.andrewandy.ascendency.serverplugin.api.ability;

public abstract class AbstractAbility implements Ability {

    private final String name;
    private final boolean isActive;

    public AbstractAbility(final String name, final boolean isActive) {
        this.name = name;
        this.isActive = isActive;
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
