package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.components;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.AbstractRune;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class RuneDivineCrown extends AbstractRune {

    private final Collection<UUID> casters = new HashSet<>();

    private final PlayerMatchManager matchManager;
    private final AbilityCircletOfTheAccused boundAbility;
    private final RuneCoupDEclat boundRune;

    @AssistedInject
    RuneDivineCrown(
            @Assisted final AbilityCircletOfTheAccused boundAbility,
            @Assisted final RuneCoupDEclat boundRune,
            final PlayerMatchManager matchManager
    ) {
        super(boundAbility.getBoundChallenger());
        this.matchManager = matchManager;
        this.boundAbility = boundAbility;
        this.boundRune = boundRune;
    }

    @Override
    public void applyTo(final Player player) {
        clearFrom(player);
        casters.add(player.getUniqueId());
    }

    @Override
    public void clearFrom(final Player player) {
        casters.remove(player.getUniqueId());
    }

    @Override
    public @NotNull String getName() {
        return "Divine Crown";
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return null;
    }

    @Override
    public void tick() {
        for (final UUID uuid : casters) {
            final Optional<CircletData> optionalCirclet = boundAbility.getCircletDataFor(uuid);
            if (!optionalCirclet.isPresent()) {
                continue;
            }
            final CircletData data = optionalCirclet.get();
            final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(uuid);
            if (!optionalPlayer.isPresent()) {
                continue;
            }
            final Player player = optionalPlayer.get();
            final Optional<Team> optionalTeam = matchManager.getTeamOf(player.getUniqueId());
            if (!optionalTeam.isPresent()) {
                return;
            }
            final Team team = optionalTeam.get();
            final Collection<Player> players =
                    Common.getEntities(Player.class, boundRune.getExtentViewFor(data), (Player p) -> {
                        final Optional<Team> optional = matchManager.getTeamOf(p.getUniqueId());
                        return optional.isPresent() && optional.get() == team && data
                                .generateCircleTest().test(p.getLocation()); //If in circle and ifallied
                    }); //Get players in circle
            for (final Player p : players) {
                final PotionEffectData peData = p.get(PotionEffectData.class).orElseThrow(
                        () -> new IllegalStateException(
                                "Unable to get potion effect data for " + p.getName()));
                peData.asList().removeIf(
                        ChallengerUtils::isEffectNegative); //Remove all negative effects as per ChallengerUtils implementation.
                peData.addElement(
                        PotionEffect.builder().potionType(PotionEffectTypes.SPEED).duration(1)
                                .amplifier(2).build()) //Speed 2
                        //.addElement((PotionEffect) new BuffEffectManaRegen(1, 2)) // Mana Regen 2 | Safe cast because of sponge's runtime "mixins"
                        .addElement(PotionEffect.builder().potionType(PotionEffectTypes.REGENERATION)
                                .amplifier(1).build()) // Regen 1
                        .addElement(
                                PotionEffect.builder().potionType(PotionEffectTypes.STRENGTH).amplifier(1)
                                        .build()) // Strength 1
                        .addElement(
                                PotionEffect.builder().potionType(PotionEffectTypes.RESISTANCE).amplifier(1)
                                        .build()); //Resistance 1
                p.offer(peData); //Give the player potion effects.
            }
        }
    }

}
