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
    private Collection<ManagedMatch> matches = new HashSet<>(); //Holds all the registered matches.

    public static void enableManager() {
        disableManager();
        Sponge.getEventManager().registerListeners(AscendencyServerPlugin.getInstance(), INSTANCE);
    }

    public static void disableManager() {
        Sponge.getEventManager().unregisterListeners(INSTANCE);
    }

    public void setResetCoordinate(Location<World> location) {
        this.resetCoordinate = location;
    }

    @Listener(order = Order.LAST) //Executed last
    public void onMatchStartConflictCheck(MatchStartEvent event) {
        Match started = event.getMatch();
        //Check for player conflicts.
        String message = "[WARNING] [Ascendency Match Manager] Detected a player conflict in a match being started which is not being tracked!";
        boolean logged = false;
        for (UUID uuid : started.getPlayers()) {
            Optional<ManagedMatch> optionalMatch = getMatchOf(uuid);
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
    public void onMatchEnd(MatchEndedEvent event) {
        Match match = event.getMatch();
        if (!(match instanceof ManagedMatch)) {
            return;
        }
        matches.remove(match);
    }

    @Listener(order = Order.FIRST) //teleports the player to a reset point if it is set.
    //Run this first, so that the queue checker will ALWAYS have the most up to date state of the player.
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        UUID player = event.getTargetEntity().getUniqueId();
        if (resetCoordinate == null) {
            return;
        }
        Player playerObj = event.getTargetEntity();
        //Teleport to the reset coordinates on join.
        if (!playerObj.setLocation(resetCoordinate)) {
            throw new IllegalStateException("Unable to teleport player to reset location!");
        }
        Optional<ManagedMatch> match = getMatchOf(player); //Check for existing games
        match.ifPresent(managedMatch -> managedMatch.rejoinPlayer(player));
    }

    @Override
    public Collection<UUID> getManagedPlayers() {
        Collection<UUID> collection = new HashSet<>();
        for (ManagedMatch match : matches) {
            collection.addAll(match.getPlayers());
        }
        return collection;
    }

    @Override
    public Collection<ManagedMatch> getRegisteredMatches() {
        return new HashSet<>(matches);
    }

    @Override
    public Optional<Team> getTeamOf(UUID player) {
        Optional<ManagedMatch> match = getMatchOf(player);
        return match.map(managedMatch -> managedMatch.getTeamOf(player));
    }

    @Override
    public Optional<ManagedMatch> getMatchOf(UUID player) {
        Objects.requireNonNull(player);
        for (ManagedMatch match : matches) {
            if (match.containsPlayer(player)) {
                return Optional.of(match);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean addPlayerToMatch(UUID player, Team team, ManagedMatch match) throws IllegalArgumentException {
        Optional<ManagedMatch> optionalCurrentMatch = getMatchOf(player);
        if (!canPlayerBeAddedToMatch(player, match)) {
            return false;
        }
        if (optionalCurrentMatch.isPresent()) {
            ManagedMatch current = optionalCurrentMatch.get();
            if (!current.removePlayer(player)) {
                throw new IllegalStateException("Unable to remove the player from their current match!");
            }
        }
        return match.addPlayer(team, player);
    }

    @Override
    public boolean removePlayerFromMatch(UUID player) {
        Optional<ManagedMatch> optional = getMatchOf(player);
        return optional.map(match -> match.removePlayer(player)).orElse(false);
    }

    @Override
    public boolean canPlayerBeAddedToMatch(UUID player, ManagedMatch newMatch) {
        if (newMatch == null) {
            return false;
        }
        Optional<ManagedMatch> optional = getMatchOf(player);
        if (optional.isPresent()) {
            ManagedMatch old = optional.get();
            if (old != newMatch) {
                return newMatch.acceptsNewPlayers() && optional.map(match -> match.isLobby() || match.isEnded()).orElse(false);
            }
            return true;
        }
        return newMatch.acceptsNewPlayers() && optional.map(match -> match.isLobby() || match.isEnded()).orElse(false);
    }

    @Override
    public boolean canMovePlayerTo(UUID player, ManagedMatch newMatch) {
        return Objects.requireNonNull(newMatch).acceptsNewPlayers();
    }

    @Override
    public void registerMatch(ManagedMatch managedMatch) {
        unregisterMatch(managedMatch);
        matches.add(managedMatch);
    }

    @Override
    public void unregisterMatch(ManagedMatch managedMatch) {
        matches.remove(managedMatch);
    }

    @Override
    public boolean startMatch(ManagedMatch managedMatch) {
        Objects.requireNonNull(managedMatch);
        if (managedMatch.canStart() || managedMatch.getState() != Match.MatchState.LOBBY) {
            return false;
        }
        for (UUID player : managedMatch.getPlayers()) {
            if (canPlayerBeAddedToMatch(player, managedMatch)) {
                return false;
            }
        }
        registerMatch(managedMatch);
        return managedMatch.start(this);
    }
}
