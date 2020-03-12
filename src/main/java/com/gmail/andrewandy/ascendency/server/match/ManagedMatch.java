package com.gmail.andrewandy.ascendency.server.match;

import com.gmail.andrewandy.ascendency.common.match.Team;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ManagedMatch extends Match {

    boolean addPlayer(Team team, UUID player);

    default boolean addPlayer(String teamName, UUID player) {
        Optional<Team> team = getTeamByName(teamName);
        if (!team.isPresent()) {
            return false;
        }
        return addPlayer(team.get(), player);
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

}
