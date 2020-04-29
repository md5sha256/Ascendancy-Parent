package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match;

import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.event.MatchEndedEvent;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.event.MatchStartEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;

/**
 * Represents the Player-MatchManager which ascendency will ALWAYS default to.
 * This manager will also track all {@link MatchStartEvent} events to check for conflicts,
 * however this manager will default to ignoring conflicts and will only log a warning to console.
 */
public enum SimplePlayerMatchManager implements PlayerMatchManager {

    INSTANCE;

    private Location<World> resetCoordinate;
    private final Collection<ManagedMatch> matches = new HashSet<>(); //Holds all the registered matches.

    public static void enableManager() {
        disableManager();
        Sponge.getEventManager().registerListeners(AscendencyServerPlugin.getInstance(), INSTANCE);
    }

    public static void disableManager() {
        Sponge.getEventManager().unregisterListeners(INSTANCE);
    }

    public void setResetCoordinate(final Location<World> location) {
        this.resetCoordinate = location;
    }

    @Listener(order = Order.LAST) //Executed last
    public void onMatchStartConflictCheck(final MatchStartEvent event) {
        final Match started = event.getMatch();
        //Check for player conflicts.
        final String message =
            "[WARNING] [Ascendency Match Manager] Detected a player conflict in a match being started which is not being tracked!";
        boolean logged = false;
        for (final UUID uuid : started.getPlayers()) {
            final Optional<ManagedMatch> optionalMatch = getMatchOf(uuid);
            if (optionalMatch.isPresent()) {
                if (optionalMatch.get() != started && !logged) {
                    System.out.println(message);
                    logged = true;
                }
            }
        }
        if (!(started instanceof ManagedMatch)) {
            return;
        }
    }

    @Listener(order = Order.LAST) //Executed last.
    public void onMatchEnd(final MatchEndedEvent event) {
        final Match match = event.getMatch();
        if (!(match instanceof ManagedMatch)) {
            return;
        }
        matches.remove(match);
    }

    @Listener(order = Order.FIRST) //teleports the player to a reset point if it is set.
    //Run this first, so that the queue checker will ALWAYS have the most up to date state of the player.
    public void onPlayerJoin(final ClientConnectionEvent.Join event) {
        final UUID player = event.getTargetEntity().getUniqueId();
        if (resetCoordinate == null) {
            return;
        }
        final Player playerObj = event.getTargetEntity();
        //Teleport to the reset coordinates on join.
        if (!playerObj.setLocation(resetCoordinate)) {
            throw new IllegalStateException("Unable to teleport player to reset location!");
        }
        final Optional<ManagedMatch> match = getMatchOf(player); //Check for existing games
        match.ifPresent(managedMatch -> managedMatch.rejoinPlayer(player));
    }

    @Override public Collection<UUID> getManagedPlayers() {
        final Collection<UUID> collection = new HashSet<>();
        for (final ManagedMatch match : matches) {
            collection.addAll(match.getPlayers());
        }
        return collection;
    }

    @Override public Collection<ManagedMatch> getRegisteredMatches() {
        return new HashSet<>(matches);
    }

    @Override public Optional<Team> getTeamOf(final UUID player) {
        final Optional<ManagedMatch> match = getMatchOf(player);
        return match.map(managedMatch -> managedMatch.getTeamOf(player));
    }

    @Override public Optional<ManagedMatch> getMatchOf(final UUID player) {
        Objects.requireNonNull(player);
        for (final ManagedMatch match : matches) {
            if (match.containsPlayer(player)) {
                return Optional.of(match);
            }
        }
        return Optional.empty();
    }

    @Override public boolean addPlayerToMatch(final UUID player, final Team team, final ManagedMatch match)
        throws IllegalArgumentException {
        final Optional<ManagedMatch> optionalCurrentMatch = getMatchOf(player);
        if (!canPlayerBeAddedToMatch(player, match)) {
            return false;
        }
        if (optionalCurrentMatch.isPresent()) {
            final ManagedMatch current = optionalCurrentMatch.get();
            if (!current.removePlayer(player)) {
                throw new IllegalStateException(
                    "Unable to remove the player from their current match!");
            }
        }
        return match.addPlayer(team, player);
    }

    @Override public boolean removePlayerFromMatch(final UUID player) {
        final Optional<ManagedMatch> optional = getMatchOf(player);
        return optional.map(match -> match.removePlayer(player)).orElse(false);
    }

    @Override public boolean canPlayerBeAddedToMatch(final UUID player, final ManagedMatch newMatch) {
        if (newMatch == null) {
            return false;
        }
        final Optional<ManagedMatch> optional = getMatchOf(player);
        if (optional.isPresent()) {
            final ManagedMatch old = optional.get();
            if (old != newMatch) {
                return newMatch.acceptsNewPlayers() && optional
                    .map(match -> match.isLobby() || match.isEnded()).orElse(false);
            }
            return true;
        }
        return newMatch.acceptsNewPlayers();
    }

    @Override public boolean canMovePlayerTo(final UUID player, final ManagedMatch newMatch) {
        return Objects.requireNonNull(newMatch).acceptsNewPlayers();
    }

    @Override public void registerMatch(final ManagedMatch managedMatch) {
        unregisterMatch(managedMatch);
        matches.add(managedMatch);
    }

    @Override public void unregisterMatch(final ManagedMatch managedMatch) {
        matches.remove(managedMatch);
    }

    @Override public boolean startMatch(final ManagedMatch managedMatch) {
        Objects.requireNonNull(managedMatch);
        if (managedMatch.canStart() || managedMatch.getState() != Match.MatchState.LOBBY) {
            return false;
        }
        for (final UUID player : managedMatch.getPlayers()) {
            if (canPlayerBeAddedToMatch(player, managedMatch)) {
                return false;
            }
        }
        registerMatch(managedMatch);
        return managedMatch.start(this);
    }
}
