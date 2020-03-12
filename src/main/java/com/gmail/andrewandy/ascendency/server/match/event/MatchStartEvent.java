package com.gmail.andrewandy.ascendency.server.match.event;

import com.gmail.andrewandy.ascendency.server.match.Match;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class MatchStartEvent extends MatchEvent {

    public MatchStartEvent(Match match) {
        super(match);
    }
}
