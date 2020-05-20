package com.gmail.andrewandy.ascendency.serverplugin.matchmaking;

import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.SimplePlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.event.MatchStartEvent;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.event.PlayerJoinMatchEvent;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.event.PlayerLeftMatchEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents a service which automatically tries so make matches based on current player queues.
 *
 * @param <M>
 */
public class MatchMakingService<M extends ManagedMatch> {

    private Queue<Player> playerQueue = new LinkedList<>();

    private int minPlayersPerGame;
    private int maxPlayersPerGame;
    private MatchMakingMode mode = MatchMakingMode.BALANCED; //The way server matches players.

    private Supplier<M> matchMakingFactory;
    private Consumer<ManagedMatch> onMatchStart; //TODO --> Add a match start handler!

    /**
     * Create a new match making service.
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

    public MatchMakingMode getMatchMakingMode() {
        return mode;
    }

    /**
     * Set the way this service will match players.
     *
     * @see MatchMakingMode
     */
    public MatchMakingService<M> setMatchMakingMode(MatchMakingMode mode) {
        mode = mode == null ? MatchMakingMode.BALANCED : mode;
        this.mode = mode;
        return this;
    }

    /**
     * Register this service's listeners with forge and sponge.
     * If the listeners are not registered, some functionality
     * may not work as intended and may cause memory leaks!
     */
    public void registerListeners() {
        unregisterListeners();
        MinecraftForge.EVENT_BUS.register(this);
        Sponge.getEventManager().registerListeners(AscendencyServerPlugin.getInstance(), this);
    }

    /**
     * Unregisters the listeners with forge and sponge.
     */
    public void unregisterListeners() {
        MinecraftForge.EVENT_BUS.unregister(this);
        Sponge.getEventManager().unregisterListeners(this);
    }

    /**
     * Clear all the players from this service's queue.
     *
     * @return Returns a clone of the current queue.
     */
    public Queue<Player> clearQueue() {
        Queue<Player> players = new LinkedList<>(playerQueue);
        playerQueue.clear();
        return players;
    }

    public int getQueueSize() {
        return playerQueue.size();
    }

    /**
     * Will try to start a new match, once it is unsuccessful,
     * this method will stop trying to start new matches.
     */
    public void tryMatch() {
        int creatableMatchCount = playerQueue.size() / minPlayersPerGame;
        int optimizedMatchCount;
        switch (mode) {
            case FASTEST:
                optimizedMatchCount = creatableMatchCount;
                break;
            case BALANCED:
                optimizedMatchCount = creatableMatchCount > 0 ? playerQueue.size() / maxPlayersPerGame : creatableMatchCount;
                break;
            case OPTIMAL:
                optimizedMatchCount = playerQueue.size() / maxPlayersPerGame;
                break;
            default:
                throw new IllegalStateException("Unknown MatchMakingMode: " + mode + " found!");
        }
        while (optimizedMatchCount > 0) {
            M match = matchMakingFactory.get();
            Collection<UUID> players = new HashSet<>(minPlayersPerGame);
            int index = 0;
            for (Player player : playerQueue) {
                if (index == maxPlayersPerGame) {
                    break;
                }
                players.add(player.getUniqueId());
                index++;
            }
            players.removeIf((player) -> new PlayerJoinMatchEvent(player, match).callEvent());
            match.addAndAssignPlayersTeams(players);
            if (new MatchStartEvent(match).callEvent()) { //If event was not cancelled.
                playerQueue.removeIf((Player player) -> players.contains(player.getUniqueId()));
                SimplePlayerMatchManager.INSTANCE.startMatch(match);
                optimizedMatchCount--;
            } else {
                break;
            }
        }
    }

    public boolean addToQueue(Player player) {
        PlayerMatchManager matchManager = SimplePlayerMatchManager.INSTANCE;
        if (matchManager.getMatchOf(player.getUniqueId()).isPresent()) {
            playerQueue.remove(player);
            return false;
        }
        if (playerQueue.contains(player)) {
            return true;
        }
        return playerQueue.add(player);
    }

    public void removeFromQueue(UUID uuid) {
        playerQueue.removeIf(player -> player.getUniqueId().equals(uuid));
    }

    public void removeFromQueue(Player player) {
        removeFromQueue(player.getUniqueId());
    }

    public void addToQueueAndTryMatch(Player player) {
        if (addToQueue(player)) {
            tryMatch();
        }
    }


    @Listener(order = Order.LAST)
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Optional<ManagedMatch> current = SimplePlayerMatchManager.INSTANCE.getMatchOf(event.getTargetEntity().getUniqueId());
        if (!current.isPresent()) {
            //If not in previous match, then try to load them into the matchmaking queue.
            addToQueueAndTryMatch(event.getTargetEntity());
            System.out.println(playerQueue);
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerDisconnect(ClientConnectionEvent.Disconnect event) {
        System.out.println(playerQueue);
        playerQueue.remove(event.getTargetEntity());
    }

    @Listener(order = Order.LAST)
    public void onPlayerLeaveMatch(PlayerLeftMatchEvent event) {
        Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(event.getPlayer());
        optionalPlayer.ifPresent(this::addToQueueAndTryMatch); //Add the player to the queue.
    }

    /**
     * Represents how this service will try to match players.
     */
    public enum MatchMakingMode {

        /**
         * Will try to match players once
         * {@link #minPlayersPerGame} is reached.
         */
        FASTEST,

        /**
         * Will see if there are enough players to
         * meet {@link #maxPlayersPerGame} if not,
         * will match based on {@link #minPlayersPerGame}
         */
        BALANCED,

        /**
         * Will ONLY match based on if there are enough players to meet
         * {@link #maxPlayersPerGame}.
         */
        OPTIMAL;

    }


}
