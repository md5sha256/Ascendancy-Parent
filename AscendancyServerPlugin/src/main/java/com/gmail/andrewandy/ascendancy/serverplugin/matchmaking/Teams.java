package com.gmail.andrewandy.ascendancy.serverplugin.matchmaking;

import com.gmail.andrewandy.ascendancy.lib.game.AscendancyTeam;
import com.gmail.andrewandy.ascendancy.lib.util.CommonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum Teams {

    ARCTIC(AscendancyTeam.ARCTIC, 1, 5),
    BLAZIC(AscendancyTeam.BLAZIC, 6, 10);

    private final AscendancyTeam ascendancyTeam;
    private final Team team = new Team(CommonUtils.capitalise(name().toLowerCase()), 3);

    Teams(final AscendancyTeam ascendancyTeam, final int min, final int max) {
        this.ascendancyTeam = ascendancyTeam;
        team.setIDs(min, max);
    }

    public static Collection<Team> createTeamList() {
        List<Team> teams = new ArrayList<>(values().length);
        for (final Teams team : values()) {
            teams.add(team.asTeamObject());
        }
        return teams;
    }

    public AscendancyTeam asAscendancyTeam() {
        return ascendancyTeam;
    }

    public Team asTeamObject() {
        return team.clone();
    }
}
