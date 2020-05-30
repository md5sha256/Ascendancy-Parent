package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match;

import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Team;

import java.util.Collection;
import java.util.UUID;

public interface Match {

    Collection<Team> getTeams();

    default boolean isEngaged() {
        return getState() == MatchState.ENGAGED;
    }

    default boolean isPaused() {
        return getState() == MatchState.PAUSED;
    }

    default boolean isLobby() {
        return getState() == MatchState.LOBBY;
    }

    default boolean isEnded() {
        return getState() == MatchState.PAUSED;
    }

    default boolean isLoading() {
        return getState() == MatchState.LOADING;
    }

    default boolean containsPlayer(final UUID uuid) {
        return getTeams().stream().anyMatch((Team team) -> team.containsPlayer(uuid));
    }

    /**
     * Whether or not this match accepts new players. By default
     * this is defined as if the match is in a lobby state.
     */
    default boolean acceptsNewPlayers() {
        return isLobby();
    }

    void rejoinPlayer(UUID player);

    Collection<UUID> getPlayers();

    MatchState getState();

    enum MatchState implements Comparable<MatchState> {

        LOBBY, LOADING, ENGAGED, ENDED, PAUSED, ERROR;

        public MatchState getNext() {
            if (isSpecialState()) {
                throw new IllegalArgumentException("This state has no relative.");
            }
            if (this == ENDED) {
                return this;
            }
            return values()[this.ordinal() + 1];
        }

        public MatchState getPrevious() {
            if (isSpecialState()) {
                throw new IllegalArgumentException("This state has no relative.");
            }
            if (this == LOBBY) {
                return this;
            }
            return values()[this.ordinal() - 1];
        }

        public boolean isSpecialState() {
            return this == PAUSED || this == ERROR;
        }
    }
}
