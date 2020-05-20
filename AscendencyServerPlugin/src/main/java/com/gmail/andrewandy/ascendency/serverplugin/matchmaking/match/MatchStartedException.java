package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match;

public class MatchStartedException extends IllegalArgumentException {
    public MatchStartedException() {
    }

    public MatchStartedException(String s) {
        super(s);
    }

    public MatchStartedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MatchStartedException(Throwable cause) {
        super(cause);
    }
}
