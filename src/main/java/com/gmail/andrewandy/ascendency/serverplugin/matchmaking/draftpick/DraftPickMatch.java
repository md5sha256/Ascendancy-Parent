package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.draftpick;

import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.Rune;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.AscendancyMatch;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.util.Common;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.*;

public class DraftPickMatch implements AscendancyMatch {

    private final UUID matchID;
    private Collection<Team> teams = new HashSet<>();
    private RuneManager runeManager = new RuneManager();
    private MatchState matchState = MatchState.LOBBY;
    private DraftPickMatchEngine engine = new DraftPickMatchEngine(this);

    private DraftPickMatch(UUID matchID) {
        this.matchID = matchID;
    }

    public DraftPickMatch(Collection<Team> teams) {
        this.matchID = UUID.randomUUID();
        for (Team team : teams) {
            Team cloned = team.clone();
            teams.add(cloned);
        }
    }

    @Override public Optional<Team> getTeamByName(String name) {
        return teams.stream().filter((Team team) -> team.getName().equalsIgnoreCase(name))
            .findAny();
    }

    public RuneManager getRuneManager() {
        return runeManager;
    }

    public boolean applyRuneTo(PlayerSpecificRune rune, UUID player) {
        return runeManager.applyRuneTo(rune, player);
    }

    public boolean removeRuneFrom(PlayerSpecificRune rune, UUID player) {
        return runeManager.removeRuneFrom(rune, player);
    }

    @Override public DraftPickMatchEngine getGameEngine() {
        return engine;
    }

    @Override public boolean addPlayer(Team team, UUID player) {
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

    @Override public boolean removePlayer(UUID player) {
        Team current = getTeamOf(player);
        if (current != null) {
            current.removePlayers(player);
        }
        return true;
    }

    @Override public void setTeamOfPlayer(UUID player, Team newTeam)
        throws IllegalArgumentException {
        if (!teams.contains(newTeam)) {
            throw new IllegalArgumentException("Team specified is not registered.");
        }
        removePlayer(player);
        newTeam.addPlayers(player);
    }

    @Override public Team getTeamOf(UUID player) throws IllegalArgumentException {
        for (Team team : teams) {
            if (team.containsPlayer(player)) {
                return team;
            }
        }
        return null;
    }

    @Override public void pause(String pauseMessage) {
        //TODO Implement pause code here.
        if (pauseMessage != null) {
            teams.forEach(team -> team.getPlayers().forEach((UUID player) -> {
                Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(player);
                optionalPlayer.ifPresent((playerObj) -> Common.tell(playerObj, pauseMessage));
            }));
        }
    }

    @Override public void stop(String endMessage) {
        engine.end();
        //TODO Implement stop code here.
    }

    @Override public void resume(String resumeMessage) {
        engine.resume();
        //Todo Implement resumption code here.
    }

    @Override public boolean canStart() {
        return !isLobby();
    }

    @Override public boolean start(PlayerMatchManager manager) {
        if (!manager.verifyMatch(this)) {
            return false;
        }
        engine.start();
        return true;
    }

    @Override public Collection<Team> getTeams() {
        Collection<Team> ret = new HashSet<>();
        for (Team team : teams) {
            ret.add(team.clone());
        }
        return ret;
    }

    @Override public void rejoinPlayer(UUID player) throws IllegalArgumentException {
        engine.rejoin(player);
    }

    private void onLoading() {
        //TODO Add the draft picker task here!
    }

    private void onEnd() {
        engine.end();
    }


    @Override public void addAndAssignPlayersTeams(Collection<UUID> players) {
        Iterator<UUID> iterator = players.iterator();
        //FIll the minimum requirements;
        int playersPerTeam = players.size() / teams.size();
        for (Team team : teams) {
            if (!iterator.hasNext()) {
                break;
            }
            int index = 0;
            while (index < playersPerTeam) {
                team.addPlayers(iterator.next());
                index++;
            }
        }
    }

    @Override public Collection<UUID> getPlayers() {
        Collection<UUID> collection = new HashSet<>();
        for (Team team : teams) {
            collection.addAll(team.getPlayers());
        }
        return collection;
    }

    @Override public MatchState getState() {
        return matchState;
    }

    @Override public UUID getMatchID() {
        return matchID;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DraftPickMatch that = (DraftPickMatch) o;
        return Objects.equals(matchID, that.matchID) && Objects.equals(teams, that.teams)
            && matchState == that.matchState;
    }

    @Override public int hashCode() {
        return Objects.hash(matchID, teams, matchState);
    }

    /**
     * Represents a manager which will update the {@link AscendencyPlayer} data
     * object with regards to runes.
     */
    public class RuneManager {
        public boolean applyRuneTo(PlayerSpecificRune rune, UUID player) {
            if (!getPlayers().contains(player)) {
                throw new IllegalArgumentException("Player is not in this match!");
            }
            if (!rune.canApplyTo(player)) {
                return false;
            }
            Optional<Player> playerObj = Sponge.getServer().getPlayer(player);
            if (!playerObj.isPresent()) {
                return false;
            }
            rune.applyTo(playerObj.get());
            return true;
        }

        public boolean removeRuneFrom(PlayerSpecificRune rune, UUID player) {
            if (!getPlayers().contains(player)) {
                throw new IllegalArgumentException("Player is not in this match!");
            }
            Optional<Player> playerObj = Sponge.getServer().getPlayer(player);
            if (!playerObj.isPresent()) {
                return false;
            }
            rune.clearFrom(playerObj.get());
            return true;
        }

        public void applyRuneToAll(PlayerSpecificRune rune) {
            for (UUID uuid : getPlayers()) {
                applyRuneTo(rune, uuid);
            }
        }

        public void removeRuneFromAll(PlayerSpecificRune rune) {
            for (UUID uuid : getPlayers()) {
                removeRuneFrom(rune, uuid);
            }
        }

        public void clearRunes(UUID player) {
            Optional<AscendencyPlayer> ascendencyPlayer = engine.getGamePlayerOf(player);
            assert ascendencyPlayer.isPresent();
            AscendencyPlayer actual = ascendencyPlayer.get();
            Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(player);
            optionalPlayer.ifPresent((playerObj) -> {
                for (Rune rune : actual.getChallenger().getRunes()) {
                    rune.clearFrom(playerObj);
                }
            });
        }

        public void clearRunesFromAll() {
            getPlayers().forEach(this::clearRunes);
        }
    }
}
