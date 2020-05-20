package com.gmail.andrewandy.ascendency.serverplugin.matchmaking;

import com.gmail.andrewandy.ascendency.lib.game.AscendencyTeam;
import com.gmail.andrewandy.ascendency.lib.packet.util.CommonUtils;

public enum Teams {

    ARCTIC(AscendencyTeam.ARCTIC, 1, 5),
    BLAZIC(AscendencyTeam.BLAZIC, 6, 10);

    private AscendencyTeam ascendencyTeam;
    private Team team = new Team(CommonUtils.capitalise(name().toLowerCase()), 3);

    Teams(AscendencyTeam ascendencyTeam, int min, int max) {
        this.ascendencyTeam = ascendencyTeam;
        team.setIDs(min, max);
    }

    public AscendencyTeam asAscendencyTeam() {
        return ascendencyTeam;
    }

    public Team asTeamObject() {
        return team.clone();
    }
}
