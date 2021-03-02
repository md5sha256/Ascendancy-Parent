package com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.draftpick;

import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.AttributeData;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.engine.GameEngine;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
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
import org.spongepowered.configurate.ConfigurationNode;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents the engine in which will execute logic for the main sequence of the
 * {@link DraftPickMatch}s. This object will hold {@link AscendancyPlayer} objects
 * in order to keep track of data which cannot be directly attributed to the player,
 * or would be inefficient to do so.
 */

//TODO integrate draft-picking with the UI
public class DraftPickMatchEngine implements GameEngine {

    private static Scoreboard scoreboard;
    private static Objective damagerObjective, victimObjective, relativeIDObjective;
    //Holds the reference to the match
    private final WeakReference<DraftPickMatch> matchReference;
    private final Collection<AscendancyPlayer> ascendancyPlayers;
    @Inject
    @Named("internal-config")
    private ConfigurationNode internalConfig;
    @Inject
    private AscendancyServerPlugin plugin;


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
    Collection<AscendancyPlayer> getAscendancyPlayers() {
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
        for (AscendancyPlayer player : ascendancyPlayers) {
            final Optional<Player> optional = Sponge.getServer().getPlayer(player.getPlayerUUID());
            // Remove attribute data on game end.
            optional.ifPresent(p -> p.remove(AttributeData.class));
        }
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
     * Internal method, initialises the scoreboard to synchronise data with
     * the commndblock implementation of this game engine.
     */
    private void initScoreBoard() {
        if (scoreboard != null) {
            return;
        }
        ConfigurationNode node = internalConfig;
        node = node.node("DamageScoreboard");
        Preconditions.checkArgument(!node.virtual() && !node.empty(), "Invalid Config! DamageScoreboard is missing!");
        final String rawName, rawDamager, rawVictim;
        rawName = node.node("ScoreboardPlayerID").getString();
        rawDamager = node.node("ScoreboardDamager").getString();
        rawVictim = node.node("ScoreboardVictim").getString();

        assert rawName != null && rawDamager != null && rawVictim != null;

        relativeIDObjective =
                Objective.builder().name(rawName).criterion(Criteria.DUMMY).build();
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
        //Player object
        final Optional<AscendancyPlayer> optional = getGamePlayerOf(player.getUniqueId());
        if (!optional.isPresent()) {
            return;
        }
        //Cancels this event if the match is not engaged.
        if (!match.isEngaged()) {
            event.setCancelled(true);
            return;
        }
        final Text victimText = ((Player) victim).getTeamRepresentation();
        final Text damagerText = player.getTeamRepresentation();
        victimObjective.getOrCreateScore(damagerText)
                .setScore(relativeIDObjective.getOrCreateScore(victimText).getScore());
        damagerObjective.getOrCreateScore(victimText)
                .setScore(relativeIDObjective.getOrCreateScore(damagerText).getScore());
    }

}
