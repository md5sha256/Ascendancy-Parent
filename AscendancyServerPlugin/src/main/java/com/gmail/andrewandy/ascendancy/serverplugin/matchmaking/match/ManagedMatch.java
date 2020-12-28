package com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match;

import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.engine.GameEngine;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.engine.GamePlayer;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ManagedMatch extends Match {

    UUID getMatchID();

    boolean addPlayer(Team team, UUID player);

    default boolean addPlayer(final String teamName, final UUID player) {
        final Optional<Team> team = getTeamByName(teamName);
        return team.filter(value -> addPlayer(value, player)).isPresent();
    }

    boolean removePlayer(UUID player);

    void setTeamOfPlayer(UUID player, Team newTeam) throws IllegalArgumentException;

    void addAndAssignPlayersTeams(Collection<UUID> players);

    void addAndAssignTeam(UUID player);

    Team getTeamOf(UUID player) throws IllegalArgumentException;

    Optional<Team> getTeamByName(String name);

    void pause(String pauseMessage);

    void stop(String endMessage);

    void resume(String resumeMessage);

    boolean start(PlayerMatchManager manager);

    boolean canStart();

    GameEngine getGameEngine();

    default Optional<? extends GamePlayer> getGamePlayerOf(final UUID player) {
        return getGameEngine().getGamePlayerOf(player);
    }

}
