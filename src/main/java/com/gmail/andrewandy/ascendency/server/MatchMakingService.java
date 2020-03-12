package com.gmail.andrewandy.ascendency.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.LinkedList;
import java.util.Queue;

public class MatchMakingService {

    private Queue<EntityPlayer> players = new LinkedList<>();

    private int minPlayersPerTeam;
    private int maxPlayersPerTeam;

    public MatchMakingService(int minPlayers, int maxPlayers) {
        if (!checkPlayerCount(minPlayers, maxPlayers)) {
            throw new IllegalArgumentException("Invalid Player limits!");
        }
        this.maxPlayersPerTeam = maxPlayers;
        this.minPlayersPerTeam = minPlayers;
    }

    private boolean checkPlayerCount(int min, int max) {
        return min > 0 && min < max;
    }

    public MatchMakingService setMaxPlayersPerTeam(int maxPlayers) {
        if (!checkPlayerCount(minPlayersPerTeam, maxPlayers)) {
            throw new IllegalArgumentException("Invalid Player limits!");
        }
        this.maxPlayersPerTeam = maxPlayers;
        return this;
    }

    public MatchMakingService setMinPlayersPerTeam(int minPlayers) {
        if (!checkPlayerCount(minPlayers, maxPlayersPerTeam)) {
            throw new IllegalStateException("Invalid Player limits!");
        }
        this.minPlayersPerTeam = minPlayers;
        return this;
    }

    private void tryMatch() {



    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        players.add(event.player);
        tryMatch();
    }

    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        players.remove(event.player);
    }

}
