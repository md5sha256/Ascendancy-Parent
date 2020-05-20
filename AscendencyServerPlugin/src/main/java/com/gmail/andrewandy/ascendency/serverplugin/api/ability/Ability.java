package com.gmail.andrewandy.ascendency.serverplugin.api.ability;

public interface Ability {

    /**
     * Get the name of this ability
     *
     * @return The name of this ability
     */
    String getName();

    /**
     * @return Returns whether this is the passive ability;
     */
    boolean isPassive();

    /**
     * @return Returns whether this is the active ability.
     */
    boolean isActive();
}
