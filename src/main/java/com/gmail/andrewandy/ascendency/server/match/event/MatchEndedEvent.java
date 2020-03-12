package com.gmail.andrewandy.ascendency.server.match.event;

import com.gmail.andrewandy.ascendency.server.match.Match;

public class MatchEndedEvent extends MatchEvent {


    public MatchEndedEvent(Match match) {
        super(match);
    }
}
