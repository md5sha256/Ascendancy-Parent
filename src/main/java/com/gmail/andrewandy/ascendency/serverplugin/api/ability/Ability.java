package com.gmail.andrewandy.ascendency.serverplugin.api.ability;

import java.util.Collection;
import java.util.UUID;

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

    /**
     * Register a player as having this ability.
     * @param player The UniqueID of the player.
     */
    void register(UUID player);

    /**
     * Unregister a player as having this ability.
     * @param player The UniqueID of the player.
     */
    void unregister(UUID player);

    /**
     * Whether a player is registered to have this ability.
     * @param player The UniqueID of the player.
     */
    boolean isRegistered(UUID player);

    /**
     * Get all players with this ability.
     * @return Returns a {@link Collection<UUID>} of the player's UniqueIDs
     */
    Collection<UUID> getRegistered();
}
