package com.gmail.andrewandy.ascendency.server.draftpick;

import com.gmail.andrewandy.ascendency.common.match.Team;
import com.gmail.andrewandy.ascendency.common.util.Common;
import com.gmail.andrewandy.ascendency.server.match.ManagedMatch;
import com.gmail.andrewandy.ascendency.server.match.PlayerMatchManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.util.*;

public class DraftPickMatch implements ManagedMatch {

    private Collection<Team> teams = new HashSet<>();
    private MatchState matchState = MatchState.LOBBY;
    private int maxPlayersPerTeam;
    private int minPlayersPerTeam = 1;

    public DraftPickMatch(int maxPlayersPerTeam) {
    }

    public DraftPickMatch(int maxPlayersPerTeam, Collection<Team> teams) {
        if (maxPlayersPerTeam > minPlayersPerTeam) {
            throw new IllegalArgumentException("Max players is invalid!");
        }
        for (Team team : teams) {
            Team cloned = team.clone();
            if (cloned.getPlayerCount() > maxPlayersPerTeam) {
                throw new IllegalArgumentException("Teams passed exceed max player count!");
            }
            teams.add(cloned);
        }
    }

    public DraftPickMatch setMaxPlayersPerTeam(int maxPlayersPerTeam) {
        if (maxPlayersPerTeam > minPlayersPerTeam) {
            throw new IllegalArgumentException("Max players is invalid!");
        }
        this.maxPlayersPerTeam = maxPlayersPerTeam;
        return this;
    }

    public DraftPickMatch setMinPlayersPerTeam(int minPlayersPerTeam) {
        if (maxPlayersPerTeam > minPlayersPerTeam) {
            throw new IllegalArgumentException("Min players is invalid!");
        }
        this.minPlayersPerTeam = minPlayersPerTeam;
        return this;
    }

    @Override
    public Optional<Team> getTeamByName(String name) {
        return teams.stream().filter((Team team) -> team.getName().equalsIgnoreCase(name)).findAny();
    }

    @Override
    public boolean addPlayer(Team team, UUID player) {
        if (!teams.contains(team)) {
            throw new IllegalArgumentException("Team specified is not registered.");
        }
        Team current = getTeamOf(player);
        if (current != null) {
            return false;
        }
        team.addPlayers(player);
        return true;
    }

    @Override
    public boolean removePlayer(UUID player) {
        Team current = getTeamOf(player);
        if (current != null) {
            current.removePlayers(player);
        }
        return true;
    }

    @Override
    public void setTeamOfPlayer(UUID player, Team newTeam) throws IllegalArgumentException {
        if (!teams.contains(newTeam)) {
            throw new IllegalArgumentException("Team specified is not registered.");
        }
        removePlayer(player);
        newTeam.addPlayers(player);
    }

    @Override
    public Team getTeamOf(UUID player) throws IllegalArgumentException {
        for (Team team : teams) {
            if (team.containsPlayer(player)) {
                return team;
            }
        }
        return null;
    }

    @Override
    public void pause(String pauseMessage) {
        //TODO Implement pause code here.
        if (pauseMessage != null) {
            PlayerList playerList = FMLServerHandler.instance().getServer().getPlayerList();
            Collection<? extends EntityPlayer> players = playerList.getPlayers();
            Map<UUID, EntityPlayer> map = new HashMap<>();
            players.forEach(p -> map.put(p.getPersistentID(), p));
            teams.forEach(team -> team.getPlayers().forEach((UUID player) -> {
                if (map.containsKey(player)) {
                    Common.tell(map.get(player), pauseMessage);
                }
            }));
        }
    }

    @Override
    public void stop(String endMessage) {
        //TODO Implement stop code here.
    }

    @Override
    public void resume(String resumeMessage) {
        //Todo Implement resumption code here.
    }

    @Override
    public boolean canStart() {
        int playerCount = 0;
        for (Team team : teams) {
            playerCount += team.getPlayerCount();
        }
        return playerCount < minPlayersPerTeam * teams.size() || !isLobby();
    }

    @Override
    public boolean start(PlayerMatchManager manager) {
        if (!manager.verifyMatch(this)) {
            return false;
        }
        //TODO Implement start code here.
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Team> getTeams() {
        Collection<Team> ret = new HashSet<>();
        for (Team team : teams) {
            ret.add(team.clone());
        }
        return ret;
    }

    @Override
    public void rejoinPlayer(UUID player) {
        //TODO Implement code.
    }

    private void onLoading() {
        //TODO Add the draft picker task here!
    }

    private void onEnd() {
        //TODO add a stats message etc.
    }

    @Override
    public void addAndAssignPlayersTeams(Collection<UUID> players) {
        Iterator<UUID> iterator = players.iterator();
        int playerCount = 0;
        //FIll the minimum requirement.
        for (Team team : teams) {
            if (!iterator.hasNext()) {
                break;
            }
            int index = 0;
            while (index < minPlayersPerTeam) {
                team.addPlayers(iterator.next());
                index++;
                playerCount++;
            }
        }
        //Fill the teams if there is still space.
        if (playerCount < maxPlayersPerTeam * teams.size()) {
            for (Team team : teams) {
                if (!iterator.hasNext()) {
                    break;
                }
                int index = team.getPlayerCount();
                while (index < maxPlayersPerTeam) {
                    team.addPlayers(iterator.next());
                    index++;
                    playerCount++;
                }
            }
        }
    }

    @Override
    public Collection<UUID> getPlayers() {
        Collection<UUID> collection = new HashSet<>();
        for (Team team : teams) {
            collection.addAll(team.getPlayers());
        }
        return collection;
    }

    @Override
    public MatchState getState() {
        return matchState;
    }
}
