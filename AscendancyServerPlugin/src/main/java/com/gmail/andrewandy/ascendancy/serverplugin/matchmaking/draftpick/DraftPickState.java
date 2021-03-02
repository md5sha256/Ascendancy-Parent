package com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.draftpick;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class DraftPickState {

    private final Collection<Challenger> banned = new HashSet<>();
    private final Map<UUID, Challenger> selected = new HashMap<>();

    public DraftPickState(final byte[] data) {
    }

    public byte[] toData() {
        return new byte[0];
    }

    public enum PickPhase {

        PICKING,
        BANNING,
        FINALISE;

    }

}
