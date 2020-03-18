package com.gmail.andrewandy.ascendencyserverplugin.matchmaking.draftpick;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents the engine in which will execute logic for the main sequence of the
 * {@link DraftPickMatch}s. This object will hold {@link AscendencyPlayer} objects
 * in order to keep track of data which cannot be directly attributed to the player,
 * or would be inefficient to do so.
 */
public class DraftPickMatchEngine {

    private Collection<AscendencyPlayer> ascendencyPlayers;

    DraftPickMatchEngine(DraftPickMatch match) {
        this.ascendencyPlayers = match.getPlayers().stream().map(AscendencyPlayer::new).collect(Collectors.toCollection(HashSet::new));
    }

    Collection<AscendencyPlayer> getAscendencyPlayers() {
        return new HashSet<>(ascendencyPlayers);
    }

    Optional<AscendencyPlayer> wrapPlayer(UUID player) {
        for (AscendencyPlayer ap : ascendencyPlayers) {
            if (ap.uuidMatches(player)) {
                return Optional.of(ap);
            }
        }
        return Optional.empty();
    }

    public void tick() {
        //TODO Update the runes, etc
    }

    public void start() {

    }

    public void pause() {

    }

    public void resume() {

    }

    public void end() {

    }

    //Register listeners for game logic.

}
