package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.draftpick;

import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;

import java.util.*;

public class DraftPickState {

    private final Collection<Challenger> banned = new HashSet<>();
    private final Map<UUID, Challenger> selected = new HashMap<>();

    public DraftPickState(final byte[] data) {
    }

    public byte[] toData() {
        return new byte[0];
    }

    public enum PickPhase {

        PICKING, BANNING, FINALISE;

    }

}
