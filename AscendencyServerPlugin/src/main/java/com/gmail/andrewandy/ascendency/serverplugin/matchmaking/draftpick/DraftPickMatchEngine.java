package com.gmail.andrewandy.ascendency.serverplugin.matchmaking.draftpick;

import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.engine.GameEngine;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Represents the engine in which will execute logic for the main sequence of the
 * {@link DraftPickMatch}s. This object will hold {@link AscendencyPlayer} objects
 * in order to keep track of data which cannot be directly attributed to the player,
 * or would be inefficient to do so.
 */

//TODO integrate draft-picking with the UI
public class DraftPickMatchEngine implements GameEngine {

    private static Scoreboard scoreboard;
    private static Objective damagerObjective, victimObjective, relativeIDObjective;
    private WeakReference<DraftPickMatch> matchReference; //Holds the reference to the match
    private Collection<AscendencyPlayer> ascendencyPlayers;


    DraftPickMatchEngine(DraftPickMatch match) {
        this.matchReference = new WeakReference<>(match);
        int index = 0;
        Collection<UUID> collection = match.getPlayers();
        this.ascendencyPlayers = new HashSet<>(collection.size());
        for (UUID uuid : collection) {
            this.ascendencyPlayers.add(new AscendencyPlayer(uuid, index++));
        }
    }

    /**
     * Get all the {@link AscendencyPlayer}s in this match.
     *
     * @return Returns a shall-cloned copy of the {@link AscendencyPlayer}s.
     */
    Collection<AscendencyPlayer> getAscendencyPlayers() {
        return new HashSet<>(ascendencyPlayers);
    }

    /**
     * Get the {@link AscendencyPlayer} object for a given player.
     *
     * @param player The UUID of the player.
     * @return Returns a populated optional or an empty optional if the player is not in this match.
     */
    @Override
    public Optional<AscendencyPlayer> getGamePlayerOf(UUID player) {
        for (AscendencyPlayer ap : ascendencyPlayers) {
            if (ap.uuidMatches(player)) {
                return Optional.of(ap);
            }
        }
        return Optional.empty();
    }

    public void start() {
        init();
        postInit();
    }

    public void pause() {

    }

    public void resume() {

    }

    public void end() {
        if (!matchReference.isEnqueued()) {
            matchReference.enqueue();
        }
        disable();
    }

    private void disable() {
        Sponge.getEventManager().unregisterListeners(this);
        ascendencyPlayers.clear();
    }

    private void init() {
        initScoreBoard();
        Sponge.getEventManager().registerListeners(AscendencyServerPlugin.getInstance(), this);
        ascendencyPlayers.forEach(this::preInitPlayer);
        DraftPickMatch match = matchReference.get();
        assert match != null;
        match.getTeams().forEach(Team::calculateIDs); //Calculate the relative ids for the players.
    }

    private void postInit() {
        ascendencyPlayers.forEach(this::postInitPlayer);
    }

    private void preInitPlayer(AscendencyPlayer player) {
        //Does nothing
    }

    private void postInitPlayer(AscendencyPlayer ascendencyPlayer) {
        ManagedMatch match = matchReference.get();
        assert match != null;
        UUID playerUID = ascendencyPlayer.getPlayerUUID();
        Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(playerUID);
        optionalPlayer.ifPresent((playerObj) -> {
            Team team = match.getTeamOf(playerUID);
            assert team != null;
            relativeIDObjective.getOrCreateScore(playerObj.getTeamRepresentation()).setScore(ascendencyPlayer.relativeID); //Set the relative ID
            Optional<Scoreboard> serverBoard = Sponge.getServer().getServerScoreboard();
            assert serverBoard.isPresent();
            Scoreboard scoreboard = serverBoard.get();
            scoreboard.registerTeam(team.getScoreboardTeam());
            team.getScoreboardTeam().addMember(playerObj.getTeamRepresentation());
        });
    }

    public void rejoin(UUID player) throws IllegalArgumentException {
        AscendencyPlayer ascendencyPlayer = getGamePlayerOf(player).orElseThrow(() -> new IllegalArgumentException("Player is not in this match!"));
        preInitPlayer(ascendencyPlayer);
    }


    /**
     * Internal method, intilaises the scoreboard to synchronise data with
     * the commndblock implementation of this game engine.
     */
    private void initScoreBoard() {
        if (scoreboard != null) {
            return;
        }
        ConfigurationNode node = AscendencyServerPlugin.getInstance().getSettings();
        node = node.getNode("DamageScoreboard");
        Objects.requireNonNull(node, "Invalid Config! DamageScoreboard is missing!");
        String rawName, rawDamager, rawVictim;
        rawName = node.getNode("ScoreboardPlayerID").getString();
        rawDamager = node.getNode("ScoreboardDamager").getString();
        rawVictim = node.getNode("ScoreboardVictim").getString();
        String relativeIDName = rawName;
        relativeIDObjective = Objective.builder().name(relativeIDName).criterion(Criteria.DUMMY).build();
        damagerObjective = Objective.builder().name(rawDamager).criterion(Criteria.DUMMY).build();
        victimObjective = Objective.builder().name(rawVictim).criterion(Criteria.DUMMY).build();
        scoreboard = Sponge.getServer().getServerScoreboard().orElseThrow(() -> new IllegalStateException("Server scoreboard not ready!"));
        scoreboard.addObjective(damagerObjective);
        scoreboard.addObjective(victimObjective);
        scoreboard.addObjective(relativeIDObjective);
    }


    //Register listeners for game logic.

    /**
     * Scoreboard updater for the command-block implementation
     * of this game engine.
     */
    @Listener(order = Order.LAST)
    public void onDamage(DamageEntityEvent event) {
        DraftPickMatch match = matchReference.get();
        if (matchReference.isEnqueued() || match == null) {
            disable();
            return;
        }
        Cause cause = event.getCause();
        Entity victim = event.getTargetEntity();
        if (!(victim instanceof Player)) {
            return;
        }
        Optional<AscendencyPlayer> optionalVictimObject = getGamePlayerOf(victim.getUniqueId());
        if (!optionalVictimObject.isPresent()) {
            return;
        }
        Player actual = null;
        List<Player> objects = cause.allOf(Player.class); //Get all players which were damaged.
        for (Player player : objects) {
            if (player == victim) {
                return;
            }
            Optional<AscendencyPlayer> optional = getGamePlayerOf(player.getUniqueId()); //Player object
            if (!optional.isPresent()) {
                continue;
            }
            if (!match.isEngaged()) { //Cancels this event if the match is not engaged.
                event.setCancelled(true);
                return;
            }
            actual = player;
            break; //Only take the first player in the list as a damager.
        }
        if (actual == null) {
            return;
        }
        Text victimText = ((Player) victim).getTeamRepresentation();
        Text damagerText = actual.getTeamRepresentation();
        victimObjective.getOrCreateScore(damagerText).setScore(relativeIDObjective.getOrCreateScore(victimText).getScore());
        damagerObjective.getOrCreateScore(victimText).setScore(relativeIDObjective.getOrCreateScore(damagerText).getScore());
    }
}
