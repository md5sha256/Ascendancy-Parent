package com.gmail.andrewandy.ascendency.server.match;

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
