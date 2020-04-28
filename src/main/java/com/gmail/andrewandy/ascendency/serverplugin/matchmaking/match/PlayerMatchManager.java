package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match;

import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.engine.GamePlayer;

import java.util.*;

/**
 * Represents a manager which manages {@link ManagedMatch}s
 */
public interface PlayerMatchManager {

    /**
     * Get all collection of all the players registered to this manager.
     */
    Collection<UUID> getManagedPlayers();

    /**
     * Get a collection of all the matches which are currently registered to this manager.
     */
    Collection<ManagedMatch> getRegisteredMatches();

    /**
     * Get the current team of a player.
     */
    Optional<Team> getTeamOf(UUID player);

    /**
     * Get the current {@link ManagedMatch} which the player is in.
     */
    Optional<ManagedMatch> getMatchOf(UUID player);


    /**
     * Check if a player is in a match which has been engaged.
     *
     * @param uuid The UUID of the player.
     * @return Whether the player is in an engaged match.
     */
    default boolean isEngaged(UUID uuid) {
        Optional<ManagedMatch> managedMatch = getMatchOf(uuid);
        return managedMatch.map(Match::isEngaged).orElse(false);
    }

    /**
     * Get all the player which are in engaged matches.
     *
     * @return A collection of players which are in engaged matches.
     */
    default Collection<UUID> getEngagedPlayers() {
        Collection<UUID> uuids = new HashSet<>();
        for (ManagedMatch managedMatch : getRegisteredMatches()) {
            if (managedMatch.isEngaged()) {
                uuids.addAll(managedMatch.getPlayers());
            }
        }
        return uuids;
    }

    /**
     * Get all the players which are in a lobby.
     *
     * @return A Map of each player to the match which is in a lobby state.
     */
    default Map<UUID, ManagedMatch> getPlayersInLobby() {
        Map<UUID, ManagedMatch> map = new HashMap<>();
        for (ManagedMatch managedMatch : getRegisteredMatches()) {
            if (managedMatch.getState() == Match.MatchState.LOBBY) {
                managedMatch.getPlayers().forEach((UUID player) -> map.put(player, managedMatch));
            }
        }
        return map;
    }

    /**
     * Attempts to move a player to a new match.
     * This method will try to move the player even is they are
     * in an engaged match!
     *
     * @param player         The UUID of the player.
     * @param team           The new team to assign the player to.
     * @param managedMatch   The match to move the player to.
     * @param removeIfFailed Whether to remove the player if the operation fails.
     * @return Returns whether the operation was successful.
     */
    default boolean movePlayerToMatch(UUID player, Team team, ManagedMatch managedMatch,
        boolean removeIfFailed) throws IllegalStateException {
        Objects.requireNonNull(managedMatch);
        Optional<ManagedMatch> previous = getMatchOf(player);
        if (previous.isPresent()) {
            if (!canMovePlayerTo(player, managedMatch)) {
                if (removeIfFailed) {
                    if (!removePlayerFromMatch(player)) {
                        throw new IllegalStateException(
                            "Unable to remove the player from the match!");
                    }
                }
                return false;
            }
            if (!removePlayerFromMatch(player)) {
                throw new IllegalStateException("Unable to remove the player from the match!");
            }
        }
        return addPlayerToMatch(player, team, managedMatch); //Return the result of the addition.
    }

    /**
     * Attempts to move a player to a match if they are in a lobby, or if they are not in any match.
     *
     * @param player       The UUID of the player.
     * @param team         The team of the player to assign to.
     * @param managedMatch The match instance to assign the player.
     * @return Returns whether the operation was successful.
     */
    default boolean moveWaitingPlayerToMatch(UUID player, Team team, ManagedMatch managedMatch) {
        Objects.requireNonNull(managedMatch);
        Optional<ManagedMatch> previous = getMatchOf(player);
        if (previous.isPresent() && previous.get().getState() != Match.MatchState.LOBBY) {
            //Return false since the player is not "waiting"
            return false;
        }
        return canPlayerBeAddedToMatch(player, managedMatch) && addPlayerToMatch(player, team,
            managedMatch);
    }

    /**
     * Add a player to a match. This method will try to move the player
     * from whichever match they are in to the new match.
     *
     * @param player The UUID of the player.
     * @param team   The team to add to.
     * @param match  The match to add to.
     * @return Returns the result of the operation. Returns false if the match was already started.
     * @throws IllegalArgumentException Thrown by: {@link ManagedMatch#setTeamOfPlayer(UUID, Team)}
     */
    boolean addPlayerToMatch(UUID player, Team team, ManagedMatch match)
        throws IllegalArgumentException;

    /**
     * Removes a player from the match they are in.
     *
     * @param player The UUID of the player.
     * @return Returns the result of the operation,
     * false if an error occurs. This method should return TRUE if the player was not in any known match.
     */
    boolean removePlayerFromMatch(UUID player);


    /**
     * Check whether a player can be added to a given match. This method checks
     * for the player is already in a running match, or if the match is started.
     *
     * @param player   The UUID of the player.
     * @param newMatch The instance of the new match.
     * @return Returns whether the player can be added to the match, based on
     * criteria laid out above.
     */
    boolean canPlayerBeAddedToMatch(UUID player, ManagedMatch newMatch);

    /**
     * Represents whether there would be a conflict if the player joined the new match. Such as,
     * if the new match was already started.
     *
     * @param player   The UUID of the player.
     * @param newMatch The instance of the new match.
     * @return Returns whether the player can be moved based on criteria discussed above.
     */
    boolean canMovePlayerTo(UUID player, ManagedMatch newMatch);

    /**
     * Asks this manager to start a given match. The manager will
     * check if any existing matches conflict with this current match's
     * supposed player count.
     * The manager will register the match if it isnt already.
     *
     * @param managedMatch The match to start.
     * @return Returns whether the match was successfully started.
     */
    boolean startMatch(ManagedMatch managedMatch);

    /**
     * Explicitly ask this manager to register this match instance
     * so that the lobby state is verified by the match manager.
     *
     * @param managedMatch The match to be managed.
     */
    void registerMatch(ManagedMatch managedMatch);

    void unregisterMatch(ManagedMatch managedMatch);

    default boolean verifyMatch(ManagedMatch managedMatch) {
        if (!managedMatch.isLobby()) {
            return false;
        }
        for (UUID player : managedMatch.getPlayers()) {
            if (getMatchOf(player).isPresent() && getMatchOf(player).get() != managedMatch) {
                return false;
            }
        }
        return true;
    }


    default Optional<? extends GamePlayer> getGamePlayerOf(UUID player) {
        Optional<ManagedMatch> optionalMatch = getMatchOf(player);
        if (!optionalMatch.isPresent()) {
            return Optional.empty();
        }
        return optionalMatch.get().getGameEngine().getGamePlayerOf(player);
    }

}
