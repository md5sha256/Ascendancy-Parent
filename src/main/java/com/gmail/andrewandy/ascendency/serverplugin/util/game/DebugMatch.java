package com.gmail.andrewandy.ascendency.serverplugin.util.game;

import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.AbstractMatch;

import java.util.Collection;

public class DebugMatch extends AbstractMatch {

    public DebugMatch() {
    }

    public DebugMatch(Collection<Team> teams) {
        super(teams);
    }



}
