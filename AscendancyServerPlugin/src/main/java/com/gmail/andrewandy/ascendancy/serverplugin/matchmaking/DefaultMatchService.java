package com.gmail.andrewandy.ascendancy.serverplugin.matchmaking;

import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.gmail.andrewandy.ascendancy.serverplugin.configuration.Config;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.event.MatchStartEvent;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.event.PlayerJoinMatchEvent;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.event.PlayerLeftMatchEvent;
import com.google.inject.Inject;
import net.minecraftforge.common.MinecraftForge;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.*;

/**
 * Represents a service which automatically tries so make matches based on current player queues.
 */
public class DefaultMatchService implements AscendancyMatchService {

    private final Config config;
    private LinkedList<Player> playerQueue = new LinkedList<>();
    private final Set<Player> playerQueueCache = new HashSet<>();
    private MatchMakingMode mode = MatchMakingMode.BALANCED; //The way server matches players.
    private MatchFactory<AscendancyMatch> matchFactory;

    @Inject
    private AscendancyServerPlugin plugin;
    @Inject
    private PlayerMatchManager matchManager;

    /**
     * Create a new match making service.
     *
     * @param matchMakingFactory A supplier for creating matches. Matches should already
     *                           be created with the correct teams AND these should be empty.
     * @param config             The config file.
     */
    @Inject
    public DefaultMatchService(final AscendancyMatchFactory matchMakingFactory,
                               final Config config) {
        this.matchFactory = matchMakingFactory;
        this.config = Objects.requireNonNull(config);
        registerListeners();
        reloadConfiguration();
    }

    public void reloadConfiguration() {
        final ConfigurationNode node = config.getRootNode().getNode("MatchMaking");
        this.mode = MatchMakingMode.valueOf(node.getNode("Mode").getString());
    }

    @Override
    public MatchFactory<AscendancyMatch> getMatchFactory() {
        return matchFactory;
    }

    @Override
    public DefaultMatchService setMatchFactory(final MatchFactory<AscendancyMatch> matchFactory) {
        this.matchFactory = matchFactory;
        return this;
    }

    @Override
    public MatchMakingMode getMatchMakingMode() {
        return mode;
    }

    /**
     * Set the way this service will match players.
     *
     * @see MatchMakingMode
     */
    @Override
    public DefaultMatchService setMatchMakingMode(MatchMakingMode mode) {
        mode = mode == null ? MatchMakingMode.BALANCED : mode;
        this.mode = mode;
        return this;
    }

    /**
     * Clear all the players from this service's queue.
     *
     * @return Returns a clone of the current queue.
     */
    @Override
    public Queue<Player> clearQueue() {
        final Queue<Player> players = this.playerQueue;
        this.playerQueue = new LinkedList<>();
        this.playerQueueCache.clear();
        return players;
    }

    @Override
    public int getQueueSize() {
        return playerQueue.size();
    }

    /**
     * Will try to start a new match, once it is unsuccessful,
     * this method will stop trying to start new matches.
     */
    @Override
    public void tryMatch() {
        final AscendancyMatch match = matchFactory.generateNewMatch();
        final int minPlayersPerGame = matchFactory.getMinPlayersPerGame(), maxPlayersPerGame =
                matchFactory.getMaxPlayersPerGame();
        final int creatableMatchCount = playerQueue.size() / minPlayersPerGame;
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
            final Collection<UUID> players = new HashSet<>(minPlayersPerGame);
            int index = 0;
            for (final Player player : playerQueue) {
                if (index == maxPlayersPerGame) {
                    break;
                }
                players.add(player.getUniqueId());
                index++;
            }
            players.removeIf((player) -> new PlayerJoinMatchEvent(player, match).callEvent());
            match.addAndAssignPlayersTeams(players);
            if (new MatchStartEvent(match).callEvent()) {
                //If event was not cancelled.
                playerQueue.removeIf((Player player) -> players.contains(player.getUniqueId()));
                playerQueueCache.removeIf((Player player) -> players.contains(player.getUniqueId()));
                matchManager.startMatch(match);
                optimizedMatchCount--;
            } else {
                break;
            }
        }
    }

    @Override
    public boolean addToQueue(final Player player) {
        if (matchManager.getMatchOf(player.getUniqueId()).isPresent()) {
            if (playerQueueCache.remove(player)) {
                playerQueue.remove(player);
            }
            return false;
        }
        if (playerQueueCache.contains(player)) {
            return true;
        }
        playerQueue.add(player);
        playerQueueCache.add(player);
        return true;
    }

    @Override
    public int getQueuePosition(final Player player) {
        return playerQueue.indexOf(player);
    }

    @Override
    public void removeFromQueue(final UUID uuid) {
        playerQueue.removeIf(player -> player.getUniqueId().equals(uuid));
        playerQueueCache.removeIf(player -> player.getUniqueId().equals(uuid));
    }

    @Override
    public void removeFromQueue(final Player player) {
        if (playerQueueCache.remove(player)) {
            playerQueue.remove(player);
        }
    }

    @Override
    public void addToQueueAndTryMatch(final Player player) {
        if (addToQueue(player)) {
            tryMatch();
        }
    }

    private boolean isInvalidPlayerCount(final int min, final int max) {
        return min <= 0 || min >= max;
    }

    /**
     * Register this service's listeners with forge and sponge.
     * If the listeners are not registered, some functionality
     * may not work as intended and may cause memory leaks!
     */
    public void registerListeners() {
        unregisterListeners();
        MinecraftForge.EVENT_BUS.register(this);
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    /**
     * Unregisters the listeners with forge and sponge.
     */
    public void unregisterListeners() {
        MinecraftForge.EVENT_BUS.unregister(this);
        Sponge.getEventManager().unregisterListeners(this);
    }

    @Listener(order = Order.LAST)
    public void onPlayerJoin(final ClientConnectionEvent.Join event) {
        final Optional<ManagedMatch> current =
                matchManager.getMatchOf(event.getTargetEntity().getUniqueId());
        if (!current.isPresent()) {
            //If not in previous match, then try to load them into the matchmaking queue.
            addToQueueAndTryMatch(event.getTargetEntity());
            //System.out.println(playerQueue);
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerDisconnect(final ClientConnectionEvent.Disconnect event) {
        //System.out.println(playerQueue);
        if (playerQueueCache.remove(event.getTargetEntity())) {
            playerQueue.remove(event.getTargetEntity());
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerLeaveMatch(final PlayerLeftMatchEvent event) {
        final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(event.getPlayer());
        optionalPlayer.ifPresent(this::addToQueueAndTryMatch); //Add the player to the queue.
    }
}
