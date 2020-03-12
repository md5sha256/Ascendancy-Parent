package com.gmail.andrewandy.ascendency.common.match;

import java.util.*;

public class Team {

    private List<UUID> players;
    private final int startingPlayerCount;
    private UUID teamUID;
    private String name;

    public Team(String name, int startSize) {
        this(name, new ArrayList<>(startSize));
    }

    public Team(String name, Collection<UUID> players) {
        teamUID = UUID.randomUUID();
        this.name = Objects.requireNonNull(name);
        this.players = new ArrayList<>(players);
        this.startingPlayerCount = players.size();
    }

    public void addPlayers(UUID... players) {
        for (UUID uuid : players) {
            this.players.remove(uuid);
            this.players.add(uuid);
        }
    }

    public void removePlayers(UUID... players) {
        for (UUID uuid : players) {
            this.players.remove(uuid);
        }
    }

    public boolean containsPlayer(UUID player) {
        return players.contains(player);
    }

    public List<UUID> getPlayers() {
        return new ArrayList<>(players);
    }

    public int getPlayerCount() {
        return players.size();
    }

    public int getStartingPlayerCount() {
        return startingPlayerCount;
    }

    public String getName() {
        return name;
    }

    public UUID getTeamUID() {
        return teamUID;
    }
}
