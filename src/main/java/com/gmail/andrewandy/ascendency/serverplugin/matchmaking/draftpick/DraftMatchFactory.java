package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.draftpick;

import com.gmail.andrewandy.ascendency.serverplugin.configuration.Config;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.AscendancyMatch;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.AscendancyMatchFactory;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Teams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ninja.leaping.configurate.ConfigurationNode;

@Singleton public class DraftMatchFactory implements AscendancyMatchFactory {

    private final Config config;
    private int minPlayersPerGame, maxPlayersPerGame;

    @Inject public DraftMatchFactory(final Config config) {
        this.config = config;
        reloadConfiguration();
    }

    public void reloadConfiguration() {
        final ConfigurationNode root = config.getRootNode();
        final ConfigurationNode node = root.getNode("MatchMaking");
        final int min = node.getNode("Min-Players").getInt();
        final int max = node.getNode("Max-Players").getInt();
        if (min > max || min < 0) {
            throw new IllegalArgumentException(
                "Invalid Min and Max players! Min must be positive and max must be greater than min.");
        }
        minPlayersPerGame = min;
        maxPlayersPerGame = max;
    }

    @Override public int getMaxPlayersPerGame() {
        return maxPlayersPerGame;
    }

    @Override public int getMinPlayersPerGame() {
        return minPlayersPerGame;
    }

    @Override public AscendancyMatch generateNewMatch() {
        return new DraftPickMatch(Teams.createTeamList());
    }


}
