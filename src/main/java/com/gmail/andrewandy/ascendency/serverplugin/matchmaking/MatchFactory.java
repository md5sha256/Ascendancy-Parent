package com.gmail.andrewandy.ascendency.serverplugin.matchmaking;

import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.Match;


public interface MatchFactory<M extends Match> {

    M generateNewMatch();

    int getMinPlayersPerGame();

    int getMaxPlayersPerGame();


}
