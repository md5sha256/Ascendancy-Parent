package com.gmail.andrewandy.ascendancy.serverplugin.matchmaking;

import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.Match;


public interface MatchFactory<M extends Match> {

    M generateNewMatch();

    int getMinPlayersPerGame();

    int getMaxPlayersPerGame();


}
