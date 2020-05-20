package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match;

import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.engine.GameEngine;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ManagedMatch extends Match {

    UUID getMatchID();

    boolean addPlayer(Team team, UUID player);

    default boolean addPlayer(String teamName, UUID player) {
        Optional<Team> team = getTeamByName(teamName);
        return team.filter(value -> addPlayer(value, player)).isPresent();
    }

    boolean removePlayer(UUID player);

    void setTeamOfPlayer(UUID player, Team newTeam) throws IllegalArgumentException;

    void addAndAssignPlayersTeams(Collection<UUID> players);

    Team getTeamOf(UUID player) throws IllegalArgumentException;

    Optional<Team> getTeamByName(String name);

    void pause(String pauseMessage);

    void stop(String endMessage);

    void resume(String resumeMessage);

    boolean start(PlayerMatchManager manager);

    boolean canStart();

    GameEngine getGameEngine();

}
