package com.gmail.andrewandy.ascendancy.serverplugin.util.game;

import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.AbstractMatch;

import java.util.Collection;
import java.util.UUID;

public class DebugMatch extends AbstractMatch {

    public DebugMatch() {
    }

    public DebugMatch(final Collection<Team> teams) {
        super(teams);
    }


    @Override
    public void addAndAssignTeam(final UUID player) {
        //TODO
    }
}
