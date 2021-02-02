package com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.draftpick;

import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.AscendancyMatch;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.AscendancyMatchFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.Teams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.spongepowered.configurate.ConfigurationNode;

@Singleton
public class DraftMatchFactory implements AscendancyMatchFactory {

    @Inject
    @Named("internal-config")
    private ConfigurationNode config;
    private int minPlayersPerGame, maxPlayersPerGame;

    public DraftMatchFactory() {
        reloadConfiguration();
    }

    public void reloadConfiguration() {
        final ConfigurationNode node = config.node("MatchMaking");
        final int min = node.node("Min-Players").getInt();
        final int max = node.node("Max-Players").getInt();
        if (min > max || min < 0) {
            throw new IllegalArgumentException(
                    "Invalid Min and Max players! Min must be positive and max must be greater than min.");
        }
        minPlayersPerGame = min;
        maxPlayersPerGame = max;
    }

    @Override
    public AscendancyMatch generateNewMatch() {
        return new DraftPickMatch(Teams.createTeamList());
    }

    @Override
    public int getMinPlayersPerGame() {
        return minPlayersPerGame;
    }

    @Override
    public int getMaxPlayersPerGame() {
        return maxPlayersPerGame;
    }


}
