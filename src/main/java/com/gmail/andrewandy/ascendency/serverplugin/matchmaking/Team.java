package com.gmail.andrewandy.ascendency.serverplugin.matchmaking;

import java.util.*;

public class Team implements Cloneable {

    private final int startingPlayerCount;
    private final transient org.spongepowered.api.scoreboard.Team team;
    private Map<UUID, Integer> relativeIDs = new HashMap<>();
    private List<UUID> players;
    private final String name;
    private int minID, maxID;

    public Team(final String name) {
        this(name, Collections.emptySet());
    }

    public Team(final String name, final Collection<UUID> players) {
        this.name = Objects.requireNonNull(name);
        this.players = new ArrayList<>(players);
        this.startingPlayerCount = players.size();
        this.team = org.spongepowered.api.scoreboard.Team.builder().name(name).build();
    }

    public org.spongepowered.api.scoreboard.Team getScoreboardTeam() {
        return team;
    }


    public void addPlayers(final UUID... players) {
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


    /**
     * Get the relative ID (command-block)
     *
     * @param player The player
     * @return Returns the relative ID of the player.
     * @throws IllegalArgumentException Thrown if {@link #containsPlayer(UUID)} returns false.
     */
    public int getRelativeID(final UUID player) {
        if (!containsPlayer(player)) {
            throw new IllegalArgumentException("Specified player is not in this team!");
        }
        final Map.Entry<?, Integer> ret =
            relativeIDs.entrySet().stream().filter((entry) -> entry.getKey().equals(player))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Relative ID not assigned!"));
        return ret.getValue();
    }

    /**
     * Calculates the relative IDs for this team instance.
     * This method mutates the current ID map.
     */
    public void calculateIDs() {
        relativeIDs.clear();
        int currentID = minID;
        if (players.size() > maxID - minID) {
            throw new IllegalArgumentException("Players size overflow!");
        }
        for (final UUID player : players) {
            relativeIDs.put(player, currentID++);
        }
        assert relativeIDs.size() == maxID - minID;
    }

    @Override public Team clone() {
        try {
            super.clone();
        } catch (final CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        final Team team = new Team(name, players);
        team.relativeIDs = new HashMap<>(this.relativeIDs);
        return team;
    }
}
