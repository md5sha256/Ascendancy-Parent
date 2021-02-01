package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.components;

import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.AbstractRune;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class RuneExpandingAgony extends AbstractRune {


    private final Collection<UUID> registered = new HashSet<>();
    private final AbilityCircletOfTheAccused boundAbility;
    private final RuneCoupDEclat boundRune;
    private final PlayerMatchManager matchManager;

    @AssistedInject
    RuneExpandingAgony(@Assisted final AbilityCircletOfTheAccused boundAbility,
                       @Assisted final RuneCoupDEclat boundRune,
                       final PlayerMatchManager matchManager) {
        super(boundAbility.getBoundChallenger());
        this.boundAbility = boundAbility;
        this.boundRune = boundRune;
        this.matchManager = matchManager;
    }

    @Override
    public void applyTo(final Player player) {
        clearFrom(player);
        this.registered.add(player.getUniqueId());
    }

    @Override
    public void clearFrom(final Player player) {
        registered.remove(player.getUniqueId());
    }

    @Override
    public String getName() {
        return "Expanding Agony";
    }

    @Override
    public void tick() {
        for (final UUID uuid : registered) {
            final Optional<CircletData> optionalCirclet = boundAbility.getCircletDataFor(uuid);
            if (!optionalCirclet.isPresent()) {
                continue;
            }
            final CircletData data = optionalCirclet.get();
            final Optional<Team> optionalTeam = matchManager.getTeamOf(uuid);
            if (!optionalTeam.isPresent()) {
                return;
            }
            final Team team = optionalTeam.get();
            final Collection<Player> players =
                    Common.getEntities(Player.class, boundRune.getExtentViewFor(data), (Player p) -> {
                        final Optional<Team> optional = matchManager.getTeamOf(p.getUniqueId());
                        return optional.isPresent() && optional.get() != team && data.isWithinCircle(p.getLocation());
                    });
            final int size = players.size();
            players.forEach((Player player) -> {
                if (size >= 2) { //If 2 enemies are in the circle
                    final PotionEffectData peData = player.get(PotionEffectData.class).orElseThrow(
                            () -> new IllegalStateException(
                                    "Unable to get potion effect data for " + player.getName()));
                    //peData.addElement((PotionEffect) new BuffEffectSilence(1, 1)); //Silence 1 | Safe cast as per sponge "mixins".
                    player.offer(peData); //Update the player.
                }
                player.offer(Keys.FIRE_TICKS, 1); //Give them 1 tick of fire.
            });
        }
    }

    @Listener
    public void onProc(final AbilityCircletOfTheAccused.ProcEvent event) {
        if (registered.contains(event.getInvoker().getUniqueId())) {
            event.setCircletRadius(6); //Change circle radius to 6.
        }
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return null;
    }
}
