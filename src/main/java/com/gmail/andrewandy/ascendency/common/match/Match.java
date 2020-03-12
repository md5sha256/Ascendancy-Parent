package com.gmail.andrewandy.ascendency.common.match;

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

    default boolean containsPlayer(UUID uuid) {
        return getTeams().stream().anyMatch((Team team) -> team.containsPlayer(uuid));
    }

    Collection<UUID> getPlayers();

    MatchState getState();

    enum MatchState implements Comparable<MatchState> {

        LOBBY, PICKING, ENGAGED, ENDED, PAUSED, ERROR;

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
