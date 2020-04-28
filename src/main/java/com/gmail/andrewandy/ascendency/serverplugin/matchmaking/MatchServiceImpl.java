package com.gmail.andrewandy.ascendency.serverplugin.matchmaking;

import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.SimplePlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.event.MatchStartEvent;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.event.PlayerJoinMatchEvent;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.event.PlayerLeftMatchEvent;
import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.*;

public class MatchServiceImpl implements AscendancyMatchService {

    private MatchFactory<AscendancyMatch> matchFactory;
    private Queue<Player> queue = new LinkedList<>();
    private MatchMakingMode matchMakingMode;
    private final PlayerMatchManager playerMatchManager;

    @Inject public MatchServiceImpl(final PlayerMatchManager playerMatchManager,
        final AscendancyMatchFactory ascendancyMatchFactory) {
        this.matchFactory = ascendancyMatchFactory;
        this.playerMatchManager = playerMatchManager;
    }

    @Override public MatchFactory<AscendancyMatch> getMatchFactory() {
        return matchFactory;
    }

    @Override public IMatchMakingService<AscendancyMatch> setMatchFactory(
        MatchFactory<AscendancyMatch> matchFactory) {
        this.matchFactory = matchFactory;
        return this;
    }

    @Override public MatchMakingMode getMatchMakingMode() {
        return matchMakingMode;
    }

    @Override public IMatchMakingService<AscendancyMatch> setMatchMakingMode(MatchMakingMode mode) {
        this.matchMakingMode = Objects.requireNonNull(mode);
        return this;
    }

    @Override public Queue<Player> clearQueue() {
        final Queue<Player> removed = new LinkedList<>(queue);
        queue.clear();
        ;
        return removed;
    }

    @Override public int getQueueSize() {
        return queue.size();
    }

    @Override public void tryMatch() {
        final int maxPlayers = matchFactory.getMaxPlayersPerGame(), minPlayers =
            matchFactory.getMaxPlayersPerGame();
        final int creatableMatchCount = (int) Math.floor(getQueueSize() / (double) minPlayers);
        int optimizedMatchCount;
        switch (matchMakingMode) {
            case FASTEST:
                optimizedMatchCount = creatableMatchCount;
                break;
            case BALANCED:
                optimizedMatchCount =
                    creatableMatchCount > 0 ? getQueueSize() / maxPlayers : creatableMatchCount;
                break;
            case OPTIMAL:
                optimizedMatchCount = getQueueSize() / maxPlayers;
                break;
            default:
                throw new IllegalStateException(
                    "Unknown MatchMakingMode: " + matchMakingMode + " found!");
        }
        while (optimizedMatchCount > 0) {
            Collection<UUID> players = new HashSet<>(minPlayers);
            int index = 0;
            for (final Player player : queue) {
                if (index == maxPlayers) {
                    break;
                }
                players.add(player.getUniqueId());
                index++;
            }
            final AscendancyMatch match = matchFactory.generateNewMatch();
            players.removeIf((player) -> new PlayerJoinMatchEvent(player, match).callEvent());
            match.addAndAssignPlayersTeams(players);
            if (new MatchStartEvent(match).callEvent()) { //If event was not cancelled.
                queue.removeIf((Player player) -> players.contains(player.getUniqueId()));
                match.start(playerMatchManager);
                optimizedMatchCount--;
            } else {
                break;
            }
        }
    }

    @Override public boolean addToQueue(Player player) {
        return queue.contains(player) && queue.add(player);
    }

    @SuppressWarnings("unchecked") @Override public int getQueuePosition(Player player) {
        return ((List<Player>) queue).indexOf(player);
    }

    @Override public void removeFromQueue(UUID uuid) {
        queue.removeIf(player -> player.getUniqueId().equals(uuid));
    }

    @Override public void removeFromQueue(Player player) {
        queue.remove(player);
    }

    @Override public void addToQueueAndTryMatch(Player player) {
        addToQueue(player);
        tryMatch();
    }

    @Listener(order = Order.LAST) public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Optional<ManagedMatch> current =
            SimplePlayerMatchManager.INSTANCE.getMatchOf(event.getTargetEntity().getUniqueId());
        if (!current.isPresent()) {
            //If not in previous match, then try to load them into the matchmaking queue.
            addToQueueAndTryMatch(event.getTargetEntity());
            System.out.println(queue);
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerDisconnect(ClientConnectionEvent.Disconnect event) {
        System.out.println(queue);
        queue.remove(event.getTargetEntity());
    }

    @Listener(order = Order.LAST) public void onPlayerLeaveMatch(PlayerLeftMatchEvent event) {
        Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(event.getPlayer());
        optionalPlayer.ifPresent(this::addToQueueAndTryMatch); //Add the player to the queue.
    }
}
