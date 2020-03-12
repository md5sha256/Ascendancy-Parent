package com.gmail.andrewandy.ascendency.common.match;

import java.util.UUID;

public interface ManagedMatch extends Match {


    boolean addPlayer(Team team, UUID player);

    boolean removePlayer(Team team, UUID player);

    void setTeamOfPlayer(UUID player, Team newTeam) throws IllegalArgumentException;

    Team getTeamOf(UUID player) throws IllegalArgumentException;

    void pause(String pauseMessage);

    void stop(String endMessage);

    void resume(String resumeMessage);

}
