package com.gmail.andrewandy.ascendency.server.match;

import com.gmail.andrewandy.ascendency.common.match.Team;
import com.gmail.andrewandy.ascendency.server.match.event.MatchEndedEvent;
import com.gmail.andrewandy.ascendency.server.match.event.MatchStartEvent;
import javafx.util.Pair;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.*;

/**
 * Represents the Player-MatchManager which ascendency will ALWAYS default to.
 * This manager will also track all {@link MatchStartEvent} events to check for conflicts,
 * however this manager will default to ignoring conflicts and will only log a warning to console.
 */
public enum SimplePlayerMatchManager implements PlayerMatchManager {

    INSTANCE;

    private Pair<int[], World> resetCoordinate; //int[] represents int[0] = x, int[1] = y, int[2] = z.
    private Collection<ManagedMatch> matches = new HashSet<>(); //Holds all the registered matches.

    public static void enableManager() {
        disableManager();
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    public static void disableManager() {
        MinecraftForge.EVENT_BUS.unregister(INSTANCE);
    }

    public void setResetCoordinate(int x, int y, int z, World world) {
        this.resetCoordinate = new Pair<>(new int[]{x, y, z}, Objects.requireNonNull(world));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST) //Executed last
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

    @SubscribeEvent(priority = EventPriority.LOWEST) //Executed last.
    public void onMatchEnd(MatchEndedEvent event) {
        Match match = event.getMatch();
        if (!(match instanceof ManagedMatch)) {
            return;
        }
        matches.remove(match);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST) //teleports the player to a reset point if it is set.
    //Run this first, so that the queue checker will ALWAYS have the most up to date state of the player.
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        UUID player = event.player.getPersistentID();
        int[] xyz;
        String worldName;
        Pair<int[], World> pair = resetCoordinate;
        ;
        if (pair == null) {
            return;
        }
        xyz = pair.getKey();

        //Teleport to the reset coordinates on join.
        event.player.setWorld(pair.getValue());
        if (!event.player.attemptTeleport(xyz[0], xyz[1], xyz[2])) {
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
