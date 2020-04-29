package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match;

public class MatchStartedException extends IllegalArgumentException {
    public MatchStartedException() {
    }

    public MatchStartedException(final String s) {
        super(s);
    }

    public MatchStartedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MatchStartedException(final Throwable cause) {
        super(cause);
    }
}
