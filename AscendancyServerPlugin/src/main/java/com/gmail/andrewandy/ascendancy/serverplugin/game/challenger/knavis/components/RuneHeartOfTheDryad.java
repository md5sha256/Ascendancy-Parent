package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.components;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.AbstractRune;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.Rune;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.engine.GameEngine;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.engine.GamePlayer;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ChangeEntityPotionEffectEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Represents the rune HeartOfTheDryad
 */
public class RuneHeartOfTheDryad extends AbstractRune {


    private final PlayerMatchManager matchManager;
    private final Map<UUID, PotionEffect[]> registered = new HashMap<>();
    private final Map<UUID, Long> currentActive = new HashMap<>();
    private final Map<UUID, Long> cooldownMap = new HashMap<>();

    @AssistedInject
    RuneHeartOfTheDryad(@Assisted final Challenger challenger, final PlayerMatchManager matchManager) {
        super(challenger);
        this.matchManager = matchManager;
    }


    @Override
    public void applyTo(final Player player) {
        clearFrom(player);
        currentActive.put(player.getUniqueId(), 0L);
        final Optional<PotionEffectData> optional = player.getOrCreate(PotionEffectData.class);
        if (!optional.isPresent()) {
            throw new IllegalStateException(
                    "Potion effect data could not be gathered for " + player.getUniqueId().toString());
        }

        final PotionEffectData data = optional.get();
        final PotionEffect[] effects = new PotionEffect[]{PotionEffect.builder()
                //Level 2 movement speed
                .potionType(PotionEffectTypes.SPEED).duration(4).amplifier(2).build(),
                PotionEffect.builder()
                        //20% Attack speed
                        .potionType(PotionEffectTypes.HASTE).duration(4).amplifier(2).build()};
        //Root / Entanglement
        for (final PotionEffect effect : effects) {
            data.addElement(effect);
        }
        player.offer(data);
        registered.put(player.getUniqueId(), effects);
        final Optional<ManagedMatch> optionalMatch = matchManager.getMatchOf(player.getUniqueId());
        optionalMatch.ifPresent(managedMatch -> {
            final GameEngine engine = managedMatch.getGameEngine();
            final Optional<? extends GamePlayer> optionalPlayer =
                    engine.getGamePlayerOf(player.getUniqueId());
            assert optionalPlayer.isPresent();
            final GamePlayer gamePlayer = optionalPlayer.get();
            final Collection<Rune> runes = gamePlayer.getRunes();
            runes.remove(this);
            runes.add(this);
        });
    }

    @Override
    public void clearFrom(final Player player) {
        currentActive.remove(player.getUniqueId());
        cooldownMap.remove(player.getUniqueId());
        final Optional<PotionEffectData> optional = player.getOrCreate(PotionEffectData.class);
        if (!optional.isPresent()) {
            throw new IllegalStateException(
                    "Potion effect data could not be gathered for " + player.getUniqueId().toString());
        }
        //Remove buffs from data
        final PotionEffectData data = optional.get();
        final PotionEffect[] effects = registered.get(player.getUniqueId());
        if (effects.length != 2) {
            return;
        }
        for (final PotionEffect potionEffect : effects) {
            data.remove(potionEffect);
        }
        player.offer(data);
        registered.replace(player.getUniqueId(), new PotionEffect[0]);
        //If player is in a match, update the GamePlayer object
        final Optional<ManagedMatch> optionalMatch = matchManager.getMatchOf(player.getUniqueId());
        optionalMatch.ifPresent(managedMatch -> {
            final GameEngine engine = managedMatch.getGameEngine();
            final Optional<? extends GamePlayer> optionalPlayer =
                    engine.getGamePlayerOf(player.getUniqueId());
            assert optionalPlayer.isPresent();
            final GamePlayer gamePlayer = optionalPlayer.get();
            final Collection<Rune> runes = gamePlayer.getRunes();
            runes.remove(this);
        });
    }

    /**
     * Reflects whether the player can have this rune applied.
     *
     * @param uuid The UUID of the player.
     * @return Returns whether the player can see noticable changes when the rune is "applied", checks
     *         for if the player already has it or if they are on cooldown.
     */
    public boolean isEligible(final UUID uuid) {
        return !currentActive.containsKey(uuid) && !cooldownMap.containsKey(uuid);
    }

    @Override
    public @NotNull String getName() {
        return "Heart Of The Dryad";
    }

    /**
     * Updates the cooldowns and actives.
     */
    @Override
    public void tick() {
        cooldownMap.entrySet()
                .removeIf(ChallengerUtils.mapTickPredicate(5L, TimeUnit.SECONDS, null));
        currentActive.entrySet()
                .removeIf(ChallengerUtils.mapTickPredicate(4L, TimeUnit.SECONDS, (UUID uuid) -> {
                    cooldownMap.put(uuid, 0L);
                    registered.compute(
                            uuid,
                            (unused, unused1) -> new PotionEffect[0]
                    ); //If player is no longer active, remove his effects
                }));
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    @NotNull
    public DataContainer toContainer() {
        return null; //TODO
    }

    @Listener
    public void onPotionApplied(final ChangeEntityPotionEffectEvent.Gain event) {
        //Check if the entity can have its this rune applied.
        if (!isEligible(event.getTargetEntity().getUniqueId())) {
            return;
        }
        final PotionEffectType effect = event.getPotionEffect().getType();

        final String name = effect.getName().toLowerCase();
        if (name.contains("fury") || effect == PotionEffectTypes.STRENGTH
                || effect == PotionEffectTypes.RESISTANCE) {
            assert event.getTargetEntity() instanceof Player;
            applyTo((Player) event.getTargetEntity());
        }
    }

}
