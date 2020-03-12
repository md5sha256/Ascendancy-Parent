package com.gmail.andrewandy.ascendency.server.match.event;

import com.gmail.andrewandy.ascendency.server.match.Match;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class MatchPauseEvent extends MatchEvent {

    public MatchPauseEvent(Match match) {
        super(match);
    }
}
