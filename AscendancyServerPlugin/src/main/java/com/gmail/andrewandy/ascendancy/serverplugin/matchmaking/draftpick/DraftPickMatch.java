package com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.draftpick;

import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.Rune;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.AscendancyMatch;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.AbstractMatch;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class DraftPickMatch extends AbstractMatch implements AscendancyMatch {

    public DraftPickMatch(final Collection<Team> teams) {
        for (final Team team : teams) {
            final Team cloned = team.clone();
            teams.add(cloned);
        }
        super.engine = new DraftPickMatchEngine(this);
    }

    @Override
    public void addAndAssignTeam(final UUID player) {
        //TODO
    }


    /**
     * Represents a manager which will update the {@link AscendancyPlayer} data
     * object with regards to runes.
     */
    public class RuneManager {
        public boolean applyRuneTo(final PlayerSpecificRune rune, final UUID player) {
            if (!getPlayers().contains(player)) {
                throw new IllegalArgumentException("Player is not in this match!");
            }
            if (!rune.canApplyTo(player)) {
                return false;
            }
            final Optional<Player> playerObj = Sponge.getServer().getPlayer(player);
            if (!playerObj.isPresent()) {
                return false;
            }
            rune.applyTo(playerObj.get());
            return true;
        }

        public boolean removeRuneFrom(final PlayerSpecificRune rune, final UUID player) {
            if (!getPlayers().contains(player)) {
                throw new IllegalArgumentException("Player is not in this match!");
            }
            final Optional<Player> playerObj = Sponge.getServer().getPlayer(player);
            if (!playerObj.isPresent()) {
                return false;
            }
            rune.clearFrom(playerObj.get());
            return true;
        }

        public void applyRuneToAll(final PlayerSpecificRune rune) {
            for (final UUID uuid : getPlayers()) {
                applyRuneTo(rune, uuid);
            }
        }

        public void removeRuneFromAll(final PlayerSpecificRune rune) {
            for (final UUID uuid : getPlayers()) {
                removeRuneFrom(rune, uuid);
            }
        }

        public void clearRunes(final UUID player) {
            final Optional<AscendancyPlayer> ascendencyPlayer =
                    ((DraftPickMatchEngine) engine).getGamePlayerOf(player);
            assert ascendencyPlayer.isPresent();
            final AscendancyPlayer actual = ascendencyPlayer.get();
            final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(player);
            optionalPlayer.ifPresent((playerObj) -> {
                for (final Rune rune : actual.getChallenger().getRunes()) {
                    rune.clearFrom(playerObj);
                }
            });
        }

        public void clearRunesFromAll() {
            getPlayers().forEach(this::clearRunes);
        }
    }
}
