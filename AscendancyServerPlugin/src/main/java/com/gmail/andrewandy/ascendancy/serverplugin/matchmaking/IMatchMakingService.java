package com.gmail.andrewandy.ascendancy.serverplugin.matchmaking;

import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.ManagedMatch;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Queue;
import java.util.UUID;

public interface IMatchMakingService<M extends ManagedMatch> {

    MatchFactory<M> getMatchFactory();

    IMatchMakingService<M> setMatchFactory(MatchFactory<M> matchFactory);

    MatchMakingMode getMatchMakingMode();

    IMatchMakingService<M> setMatchMakingMode(MatchMakingMode mode);

    Queue<Player> clearQueue();

    int getQueueSize();

    void tryMatch();

    boolean addToQueue(Player player);

    int getQueuePosition(Player player);

    void removeFromQueue(UUID uuid);

    void removeFromQueue(Player player);

    void addToQueueAndTryMatch(Player player);


    /**
     * Represents how this service will try to match players.
     */
    public enum MatchMakingMode {

        /**
         * Will try to match players once
         * as fast as possible.
         */
        FASTEST,

        /**
         * Will match players based on player counts, sometimes fast
         * sometimes optimal.
         */
        BALANCED,

        /**
         * Will ONLY match based on if there are enough players
         * to meet a game's require player count.
         */
        OPTIMAL;

    }
}
