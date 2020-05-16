package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match;

import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.engine.GameEngine;
import com.gmail.andrewandy.ascendency.serverplugin.util.Common;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.*;

public abstract class AbstractMatch implements ManagedMatch {


    public final UUID matchID = UUID.randomUUID();
    protected GameEngine engine;
    private final Collection<Team> teams = new HashSet<>();
    private Match.MatchState matchState = Match.MatchState.LOBBY;

    protected AbstractMatch() {
    }

    public AbstractMatch(Collection<Team> teams) {
        this.teams.addAll(teams);
    }

    public AbstractMatch(GameEngine engine) {
        this.engine = engine;
    }

    public Optional<Team> getTeamByName(final String name) {
        return teams.stream().filter((Team team) -> team.getName().equalsIgnoreCase(name))
            .findAny();
    }

    public GameEngine getGameEngine() {
        return engine;
    }

    public boolean addPlayer(final Team team, final UUID player) {
        if (!teams.contains(team)) {
            throw new IllegalArgumentException("Team specified is not registered.");
        }
        final Team current = getTeamOf(player);
        if (current != null) {
            return false;
        }
        team.addPlayers(player);
        return true;
    }

    public boolean removePlayer(final UUID player) {
        final Team current = getTeamOf(player);
        if (current != null) {
            current.removePlayers(player);
        }
        return true;
    }

    public void setTeamOfPlayer(final UUID player, final Team newTeam)
        throws IllegalArgumentException {
        if (!teams.contains(newTeam)) {
            throw new IllegalArgumentException("Team specified is not registered.");
        }
        removePlayer(player);
        newTeam.addPlayers(player);
    }

    public Team getTeamOf(final UUID player) throws IllegalArgumentException {
        for (final Team team : teams) {
            if (team.containsPlayer(player)) {
                return team;
            }
        }
        return null;
    }

    public void pause(final String pauseMessage) {
        //TODO Implement pause code here.
        if (pauseMessage != null) {
            teams.forEach(team -> team.getPlayers().forEach((UUID player) -> {
                final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(player);
                optionalPlayer.ifPresent((playerObj) -> Common.tell(playerObj, pauseMessage));
            }));
        }
    }

    public void stop(final String endMessage) {
        engine.end();
        //TODO Implement stop code here.
    }

    public void resume(final String resumeMessage) {
        engine.resume();
        //Todo Implement resumption code here.
    }

    public boolean canStart() {
        return !isLobby();
    }

    public boolean start(final PlayerMatchManager manager) {
        if (!manager.verifyMatch(this)) {
            return false;
        }
        engine.start();
        return true;
    }

    public Collection<Team> getTeams() {
        final Collection<Team> ret = new HashSet<>();
        for (final Team team : teams) {
            ret.add(team.clone());
        }
        return ret;
    }

    public void rejoinPlayer(final UUID player) throws IllegalArgumentException {
        engine.rejoin(player);
    }

    private void onLoading() {
        //TODO Add the draft picker task here!
    }

    private void onEnd() {
        engine.end();
    }

    public void addAndAssignPlayersTeams(final Collection<UUID> players) {
        final Iterator<UUID> iterator = players.iterator();
        //FIll the minimum requirements;
        final int playersPerTeam = players.size() / teams.size();
        for (final Team team : teams) {
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

    public Collection<UUID> getPlayers() {
        final Collection<UUID> collection = new HashSet<>();
        for (final Team team : teams) {
            collection.addAll(team.getPlayers());
        }
        return collection;
    }

    public Match.MatchState getState() {
        return matchState;
    }

    public UUID getMatchID() {
        return matchID;
    }

    @Override public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final AbstractMatch that = (AbstractMatch) o;
        return Objects.equals(matchID, that.matchID) && Objects.equals(teams, that.teams)
            && matchState == that.matchState;
    }

    @Override public int hashCode() {
        return Objects.hash(matchID, teams, matchState);
    }
}
