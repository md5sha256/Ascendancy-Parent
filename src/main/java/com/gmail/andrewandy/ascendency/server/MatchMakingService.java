package com.gmail.andrewandy.ascendency.server;

import com.gmail.andrewandy.ascendency.server.match.ManagedMatch;
import com.gmail.andrewandy.ascendency.server.match.SimplePlayerMatchManager;
import com.gmail.andrewandy.ascendency.server.match.event.MatchStartEvent;
import com.gmail.andrewandy.ascendency.server.match.event.PlayerJoinMatchEvent;
import com.gmail.andrewandy.ascendency.server.match.event.PlayerLeftMatchEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.util.*;
import java.util.function.Supplier;

/**
 * Represents a service which automatically tries so make matches based on current player queues.
 *
 * @param <M>
 */
public class MatchMakingService<M extends ManagedMatch> {

    private Queue<EntityPlayer> playerQueue = new LinkedList<>();

    private int minPlayersPerGame;
    private int maxPlayersPerGame;

    private Supplier<M> matchMakingFactory;

    /**
     * Create a nnew match making service.
     *
     * @param minPlayers         The min players per game.
     * @param maxPlayers         The max players per game.
     * @param matchMakingFactory A supplier for creating matches. Matches should already
     *                           be created with the correct teams AND these should be empty.
     */
    public MatchMakingService(int minPlayers, int maxPlayers, Supplier<M> matchMakingFactory) {
        if (isInvalidPlayerCount(minPlayers, maxPlayers)) {
            throw new IllegalArgumentException("Invalid Player limits!");
        }
        this.matchMakingFactory = Objects.requireNonNull(matchMakingFactory);
        this.maxPlayersPerGame = maxPlayers;
        this.minPlayersPerGame = minPlayers;
    }

    private boolean isInvalidPlayerCount(int min, int max) {
        return min <= 0 || min >= max;
    }

    /**
     * Set the max players per game when making new matches.
     *
     * @param maxPlayers The max players to allocate.
     * @throws IllegalArgumentException Thrown if the player limits are invalid.
     */
    public MatchMakingService<M> setMaxPlayersPerGame(int maxPlayers) {
        if (isInvalidPlayerCount(minPlayersPerGame, maxPlayers)) {
            throw new IllegalArgumentException("Invalid Player limits!");
        }
        this.maxPlayersPerGame = maxPlayers;
        return this;
    }

    /**
     * Set the max players per game when making new matches.
     *
     * @param minPlayers The min players to allocate.
     * @throws IllegalArgumentException Thrown if the player limits are invalid.
     */
    public MatchMakingService<M> setMinPlayersPerGame(int minPlayers) {
        if (isInvalidPlayerCount(minPlayers, maxPlayersPerGame)) {
            throw new IllegalStateException("Invalid Player limits!");
        }
        this.minPlayersPerGame = minPlayers;
        return this;
    }

    public void registerListeners() {
        unregisterListeners();
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void unregisterListeners() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    /**
     * Will try to start a new match, once it is unsuccessful,
     * this method will stop trying to start new matches.
     */
    private void tryMatch() {
        int creatableMatchCount = playerQueue.size() / minPlayersPerGame;
        int optimizedMatchCount = creatableMatchCount > 0 ? playerQueue.size() / maxPlayersPerGame : creatableMatchCount;
        while (optimizedMatchCount > 0) {
            M match = matchMakingFactory.get();
            Collection<UUID> players = new HashSet<>(minPlayersPerGame);
            int index = 0;
            for (EntityPlayer player : playerQueue) {
                if (index == maxPlayersPerGame) {
                    break;
                }
                players.add(player.getPersistentID());
                index++;
            }
            players.removeIf((player) -> new PlayerJoinMatchEvent(player, match).callEvent());
            match.addAndAssignPlayersTeams(players);
            if (new MatchStartEvent(match).callEvent()) {
                playerQueue.removeIf((EntityPlayer ep) -> players.contains(ep.getPersistentID()));
                SimplePlayerMatchManager.INSTANCE.startMatch(match);
                optimizedMatchCount--;
            } else {
                break;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Optional<ManagedMatch> current = SimplePlayerMatchManager.INSTANCE.getMatchOf(event.player.getPersistentID());
        if (!current.isPresent()) {
            //If not in previous match, then try to load them into the matchmaking queue.
            playerQueue.add(event.player);
            tryMatch();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerLeaveMatch(PlayerLeftMatchEvent event) {
        PlayerList playerList = FMLServerHandler.instance().getServer().getPlayerList();
        Collection<? extends EntityPlayer> players = playerList.getPlayers();
        Optional<? extends EntityPlayer> optional = players.stream().filter((p) -> p.getPersistentID().equals(event.getPlayer())).findAny();
        optional.ifPresent(playerQueue::add); //Add the player to the queue.
    }


}
