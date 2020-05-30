package com.gmail.andrewandy.ascendency.serverplugin.matchmaking;

import com.gmail.andrewandy.ascendency.lib.game.AscendencyTeam;
import com.gmail.andrewandy.ascendency.lib.util.CommonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum Teams {

    ARCTIC(AscendencyTeam.ARCTIC, 1, 5), BLAZIC(AscendencyTeam.BLAZIC, 6, 10);

    private final AscendencyTeam ascendencyTeam;
    private final Team team = new Team(CommonUtils.capitalise(name().toLowerCase()), 3);

    Teams(final AscendencyTeam ascendencyTeam, final int min, final int max) {
        this.ascendencyTeam = ascendencyTeam;
        team.setIDs(min, max);
    }

    public static Collection<Team> createTeamList() {
        List<Team> teams = new ArrayList<>(values().length);
        for (final Teams team : values()) {
            teams.add(team.asTeamObject());
        }
        return teams;
    }

    public AscendencyTeam asAscendencyTeam() {
        return ascendencyTeam;
    }

    public Team asTeamObject() {
        return team.clone();
    }
}
