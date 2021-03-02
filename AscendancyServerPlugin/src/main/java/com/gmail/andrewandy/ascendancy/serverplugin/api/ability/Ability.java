package com.gmail.andrewandy.ascendancy.serverplugin.api.ability;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;

import java.util.Collection;
import java.util.UUID;

public interface Ability {

    /**
     * @return Returns the challenger object this ability is bounded to.
     */
    Challenger getBoundChallenger();

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

    /**
     * Register a player as having this ability.
     *
     * @param player The UniqueID of the player.
     */
    void register(UUID player);

    /**
     * Unregister a player as having this ability.
     *
     * @param player The UniqueID of the player.
     */
    void unregister(UUID player);

    /**
     * Whether a player is registered to have this ability.
     *
     * @param player The UniqueID of the player.
     */
    boolean isRegistered(UUID player);

    /**
     * Get all players with this ability.
     *
     * @return Returns a {@link Collection<UUID>} of the player's UniqueIDs
     */
    Collection<UUID> getRegistered();

}
