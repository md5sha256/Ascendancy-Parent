package com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.draftpick;

import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.engine.GameEngine;
import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Represents the engine in which will execute logic for the main sequence of the
 * {@link DraftPickMatch}s. This object will hold {@link AscendancyPlayer} objects
 * in order to keep track of data which cannot be directly attributed to the player,
 * or would be inefficient to do so.
 */

//TODO integrate draft-picking with the UI
public class DraftPickMatchEngine implements GameEngine {

    @Inject
    private static AscendancyServerPlugin plugin;

    private static Scoreboard scoreboard;
    private static Objective damagerObjective, victimObjective, relativeIDObjective;
    private final WeakReference<DraftPickMatch> matchReference; //Holds the reference to the match
    private final Collection<AscendancyPlayer> ascendancyPlayers;


    DraftPickMatchEngine(final DraftPickMatch match) {
        this.matchReference = new WeakReference<>(match);
        int index = 0;
        final Collection<UUID> collection = match.getPlayers();
        this.ascendancyPlayers = new HashSet<>(collection.size());
        for (final UUID uuid : collection) {
            this.ascendancyPlayers.add(new AscendancyPlayer(uuid, index++));
        }
    }

    /**
     * Get all the {@link AscendancyPlayer}s in this match.
     *
     * @return Returns a shall-cloned copy of the {@link AscendancyPlayer}s.
     */
    Collection<AscendancyPlayer> getAscendencyPlayers() {
        return new HashSet<>(ascendancyPlayers);
    }

    public void start() {
        init();
        postInit();
    }

    public void end() {
        if (!matchReference.isEnqueued()) {
            matchReference.enqueue();
        }
        disable();
    }

    public void resume() {

    }

    public void rejoin(final UUID player) throws IllegalArgumentException {
        final AscendancyPlayer ascendancyPlayer = getGamePlayerOf(player)
                .orElseThrow(() -> new IllegalArgumentException("Player is not in this match!"));
        preInitPlayer(ascendancyPlayer);
    }

    /**
     * Get the {@link AscendancyPlayer} object for a given player.
     *
     * @param player The UUID of the player.
     * @return Returns a populated optional or an empty optional if the player is not in this match.
     */
    @Override
    public Optional<AscendancyPlayer> getGamePlayerOf(final UUID player) {
        for (final AscendancyPlayer ap : ascendancyPlayers) {
            if (ap.uuidMatches(player)) {
                return Optional.of(ap);
            }
        }
        return Optional.empty();
    }

    @Override
    public Collection<Player> getPlayersOfChallenger(final Challenger challenger) {
        final Collection<Player> collection = new HashSet<>(ascendancyPlayers.size());
        for (final AscendancyPlayer ascendancyPlayer : ascendancyPlayers) {
            if (ascendancyPlayer.getChallenger().equals(challenger)) {
                final Optional<Player> optionalPlayer =
                        Sponge.getServer().getPlayer(ascendancyPlayer.getPlayerUUID());
                optionalPlayer.ifPresent(collection::add);
            }
        }
        return collection;
    }

    public void pause() {

    }

    private void disable() {
        Sponge.getEventManager().unregisterListeners(this);
        ascendancyPlayers.clear();
    }

    private void init() {
        initScoreBoard();
        Sponge.getEventManager().registerListeners(plugin, this);
        ascendancyPlayers.forEach(this::preInitPlayer);
        final DraftPickMatch match = matchReference.get();
        assert match != null;
        //match.getTeams().forEach(Team::calculateIDs); //Calculate the relative ids for the players.
    }

    private void postInit() {
        ascendancyPlayers.forEach(this::postInitPlayer);
    }

    private void preInitPlayer(final AscendancyPlayer player) {
        //Does nothing
    }

    private void postInitPlayer(final AscendancyPlayer ascendancyPlayer) {
        final ManagedMatch match = matchReference.get();
        assert match != null;
        final UUID playerUID = ascendancyPlayer.getPlayerUUID();
        final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(playerUID);
        optionalPlayer.ifPresent((playerObj) -> {
            final Team team = match.getTeamOf(playerUID);
            assert team != null;
            relativeIDObjective.getOrCreateScore(playerObj.getTeamRepresentation())
                    .setScore(ascendancyPlayer.relativeID); //Set the relative ID
            final Optional<Scoreboard> serverBoard = Sponge.getServer().getServerScoreboard();
            assert serverBoard.isPresent();
            final Scoreboard scoreboard = serverBoard.get();
            //scoreboard.registerTeam(team.getScoreboardTeam());
            //team.getScoreboardTeam().addMember(playerObj.getTeamRepresentation());
        });
    }

    /**
     * Internal method, intilaises the scoreboard to synchronise data with
     * the commndblock implementation of this game engine.
     */
    private void initScoreBoard() {
        if (scoreboard != null) {
            return;
        }
        ConfigurationNode node = plugin.getSettings();
        node = node.getNode("DamageScoreboard");
        Objects.requireNonNull(node, "Invalid Config! DamageScoreboard is missing!");
        final String rawName, rawDamager, rawVictim;
        rawName = node.getNode("ScoreboardPlayerID").getString();
        rawDamager = node.getNode("ScoreboardDamager").getString();
        rawVictim = node.getNode("ScoreboardVictim").getString();
        final String relativeIDName = rawName;
        relativeIDObjective =
                Objective.builder().name(relativeIDName).criterion(Criteria.DUMMY).build();
        damagerObjective = Objective.builder().name(rawDamager).criterion(Criteria.DUMMY).build();
        victimObjective = Objective.builder().name(rawVictim).criterion(Criteria.DUMMY).build();
        scoreboard = Sponge.getServer().getServerScoreboard()
                .orElseThrow(() -> new IllegalStateException("Server scoreboard not ready!"));
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
    public void onDamage(final DamageEntityEvent event) {
        final DraftPickMatch match = matchReference.get();
        if (matchReference.isEnqueued() || match == null) {
            disable();
            return;
        }
        final Entity victim = event.getTargetEntity();
        if (!(victim instanceof Player)) {
            return;
        }
        final Optional<AscendancyPlayer> optionalVictimObject =
                getGamePlayerOf(victim.getUniqueId());
        if (!optionalVictimObject.isPresent()) {
            return;
        }
        final Optional<Player> optionalPlayer =
                event.getCause().get(DamageEntityEvent.CREATOR, UUID.class)
                        .flatMap(Sponge.getServer()::getPlayer);
        if (!optionalPlayer.isPresent()) {
            return;
        }
        final Player player = optionalPlayer.get();
        final Optional<AscendancyPlayer> optional =
                getGamePlayerOf(player.getUniqueId()); //Player object
        if (!optional.isPresent()) {
            return;
        }
        if (!match.isEngaged()) { //Cancels this event if the match is not engaged.
            event.setCancelled(true);
            return;
        }
        final Text victimText = ((Player) victim).getTeamRepresentation();
        final Text damagerText = player.getTeamRepresentation();
        victimObjective.getOrCreateScore(damagerText).

                setScore(relativeIDObjective.getOrCreateScore(victimText).

                        getScore());
        damagerObjective.getOrCreateScore(victimText).

                setScore(relativeIDObjective.getOrCreateScore(damagerText).

                        getScore());
    }
}
