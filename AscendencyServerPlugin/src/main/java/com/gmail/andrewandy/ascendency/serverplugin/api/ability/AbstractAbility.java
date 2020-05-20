package com.gmail.andrewandy.ascendency.serverplugin.api.ability;

public abstract class AbstractAbility implements Ability {

    private String name;
    private boolean isActive;

    public AbstractAbility(String name, boolean isActive) {
        this.name = name;
        this.isActive = isActive;
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
    public String getName() {
        return name;
    }
}
