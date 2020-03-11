package com.gmail.andrewandy.ascendency.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.Collection;
import java.util.HashSet;

public class MatchMakingService {

    private Collection<EntityPlayer> players = new HashSet<>();

    private int minPlayersPerTeam;
    private int maxPlayersPerTeam;

    public MatchMakingService setMaxPlayersPerTeam(int maxPlayersPerTeam) {
        this.maxPlayersPerTeam = maxPlayersPerTeam;
        return this;
    }

    public MatchMakingService setMinPlayersPerTeam(int minPlayersPerTeam) {
        this.minPlayersPerTeam = minPlayersPerTeam;
        return this;
    }

    private void tryMatch() {
        if (minPlayersPerTeam == 0 || minPlayersPerTeam > maxPlayersPerTeam) {
            throw new IllegalStateException("Player limits are unset!");
        }
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
