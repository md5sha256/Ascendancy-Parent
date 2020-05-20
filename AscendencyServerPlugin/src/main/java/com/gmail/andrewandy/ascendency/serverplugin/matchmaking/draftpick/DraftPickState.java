package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.draftpick;

import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;

import java.util.*;

public class DraftPickState {

    private Collection<Challenger> banned = new HashSet<>();
    private Map<UUID, Challenger> selected = new HashMap<>();

    public DraftPickState(byte[] data) {
    }

    public byte[] toData() {
        return new byte[0];
    }

    public enum PickPhase {

        PICKING, BANNING, FINALISE;

    }

}
