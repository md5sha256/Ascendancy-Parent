package com.gmail.andrewandy.ascendency.serverplugin.matchmaking;

import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.configuration.Config;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.SimplePlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.event.MatchStartEvent;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.event.PlayerJoinMatchEvent;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.event.PlayerLeftMatchEvent;
import com.google.inject.Inject;
import net.minecraftforge.common.MinecraftForge;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a service which automatically tries so make matches based on current player queues.
 */
public class DefaultMatchService implements AscendancyMatchService {

    private LinkedList<Player> playerQueue = new LinkedList<>();

    private MatchMakingMode mode = MatchMakingMode.BALANCED; //The way server matches players.

    private MatchFactory<AscendancyMatch> matchFactory;
    private final Config config;

    /**
     * Create a new match making service.
     *
     * @param matchMakingFactory A supplier for creating matches. Matches should already
     *                           be created with the correct teams AND these should be empty.
     * @param config             The config file.
     */
    @Inject public DefaultMatchService(AscendancyMatchFactory matchMakingFactory, Config config) {
        this.matchFactory = matchMakingFactory;
        this.config = Objects.requireNonNull(config);
        registerListeners();
        reloadConfiguration();
    }

    public void reloadConfiguration() {
        ConfigurationNode node = config.getRootNode().getNode("MatchMaking");
        this.mode = MatchMakingMode.valueOf(node.getNode("Mode").getString());
    }

    @Override public MatchFactory<AscendancyMatch> getMatchFactory() {
        return matchFactory;
    }

    @Override
    public DefaultMatchService setMatchFactory(MatchFactory<AscendancyMatch> matchFactory) {
        this.matchFactory = matchFactory;
        return this;
    }

    private boolean isInvalidPlayerCount(int min, int max) {
        return min <= 0 || min >= max;
    }


    @Override public MatchMakingMode getMatchMakingMode() {
        return mode;
    }

    /**
     * Set the way this service will match players.
     *
     * @see MatchMakingMode
     */
    @Override public DefaultMatchService setMatchMakingMode(MatchMakingMode mode) {
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
    @Override public Queue<Player> clearQueue() {
        Queue<Player> players = new LinkedList<>(playerQueue);
        playerQueue.clear();
        return players;
    }

    @Override public int getQueueSize() {
        return playerQueue.size();
    }

    /**
     * Will try to start a new match, once it is unsuccessful,
     * this method will stop trying to start new matches.
     */
    @Override public void tryMatch() {
        AscendancyMatch match = matchFactory.generateNewMatch();
        int minPlayersPerGame = matchFactory.getMinPlayersPerGame(), maxPlayersPerGame = matchFactory.getMaxPlayersPerGame();
        int creatableMatchCount = playerQueue.size() / minPlayersPerGame;
        int optimizedMatchCount;
        switch (mode) {
            case FASTEST:
                optimizedMatchCount = creatableMatchCount;
                break;
            case BALANCED:
                optimizedMatchCount = creatableMatchCount > 0 ?
                    playerQueue.size() / maxPlayersPerGame :
                    creatableMatchCount;
                break;
            case OPTIMAL:
                optimizedMatchCount = playerQueue.size() / maxPlayersPerGame;
                break;
            default:
                throw new IllegalStateException("Unknown MatchMakingMode: " + mode + " found!");
        }
        while (optimizedMatchCount > 0) {
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

    @SuppressWarnings("unchecked") @Override public int getQueuePosition(Player player) {
        return playerQueue.indexOf(player);
    }

    @Override public boolean addToQueue(Player player) {
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

    @Override public void removeFromQueue(UUID uuid) {
        playerQueue.removeIf(player -> player.getUniqueId().equals(uuid));
    }

    @Override public void removeFromQueue(Player player) {
        removeFromQueue(player.getUniqueId());
    }

    @Override public void addToQueueAndTryMatch(Player player) {
        if (addToQueue(player)) {
            tryMatch();
        }
    }


    @Listener(order = Order.LAST) public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Optional<ManagedMatch> current =
            SimplePlayerMatchManager.INSTANCE.getMatchOf(event.getTargetEntity().getUniqueId());
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

    @Listener(order = Order.LAST) public void onPlayerLeaveMatch(PlayerLeftMatchEvent event) {
        Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(event.getPlayer());
        optionalPlayer.ifPresent(this::addToQueueAndTryMatch); //Add the player to the queue.
    }
}
