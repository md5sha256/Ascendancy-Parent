package com.gmail.andrewandy.ascendancy.serverplugin.matchmaking;

import java.util.*;

public class Team implements Cloneable {
    private final int startingPlayerCount;
    private final List<UUID> players;
    private final String name;
    private int minID;
    private int maxID;

    public Team(final String name, final int startSize) {
        this(name, new ArrayList<>(startSize));
    }

    public Team(final String name, final Collection<UUID> players) {
        this.name = Objects.requireNonNull(name);
        this.players = new ArrayList<>(players);
        this.startingPlayerCount = players.size();
    }


    public void addPlayers(final UUID... players) {
        for (final UUID uuid : players) {
            this.players.remove(uuid);
            this.players.add(uuid);
        }
        removePlayers(players);
        this.players.addAll(Arrays.asList(players));
    }

    public void removePlayers(final UUID... players) {
        for (final UUID uuid : players) {
            this.players.remove(uuid);
        }
    }

    public void removePlayers(final Iterable<UUID> players) {
        players.forEach(this.players::remove);
    }

    public void setIDs(final int maxID, final int minID) {
        this.maxID = Math.min(maxID, minID);
        this.minID = Math.max(maxID, minID);
    }

    /**
     * @return Returns the max relative ID this team has.
     */
    public int getMaxID() {
        return maxID;
    }

    /**
     * @return Returns the min relative ID this team has.
     */
    public int getMinID() {
        return minID;
    }

    /**
     * Check if this team contain said player.
     *
     * @param player The player to check.
     * @return Returns true if the player is registered in this team, false otherwise.
     */
    public boolean containsPlayer(final UUID player) {
        return players.contains(player);
    }

    /**
     * Get all the players registered to this team.
     *
     * @return Returns a shallow-copy of the registered players.
     */
    public List<UUID> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * @return The total number of players currently in this team.
     */
    public int getPlayerCount() {
        return players.size();
    }

    /**
     * @return The total number of players the team started with.
     */
    public int getStartingPlayerCount() {
        return startingPlayerCount;
    }

    /**
     * @return The name of this team.
     */
    public String getName() {
        return name;
    }


    @Override
    public Team clone() {
        try {
            super.clone();
        } catch (final CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        final Team team = new Team(name, startingPlayerCount);
        team.players.addAll(this.players);
        return team;
    }
}
