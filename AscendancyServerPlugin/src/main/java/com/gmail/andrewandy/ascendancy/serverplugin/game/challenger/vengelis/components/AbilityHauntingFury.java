package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.vengelis.components;

import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractCooldownAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.Knavis;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.PlayerMatchManager;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DamageEntityEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Represents the HauntingFury Ability - The cooldown of this
 * ability is actually the time before the player's attacks are
 * cleared.
 */
public class AbilityHauntingFury extends AbstractCooldownAbility {

    private final PlayerMatchManager matchManager;
    private final Knavis knavis;
    private final Map<UUID, Integer> hitMap = new HashMap<>();

    @AssistedInject
    AbilityHauntingFury(@Assisted final Challenger toBind,
                        final PlayerMatchManager matchManager,
                        final Knavis knavis) {
        super("HauntingFury", false, 6, TimeUnit.SECONDS, toBind);
        super.setTickHandler(ChallengerUtils.mapTickPredicate(6, TimeUnit.SECONDS,
                this::scheduleUnregisterNextTick));
        this.matchManager = matchManager;
        this.knavis = knavis;
    }

    @Override
    public void register(final UUID player) {
        if (hitMap.containsKey(player)) {
            return;
        }
        hitMap.put(player, 0);
    }

    @Override
    public void unregister(final UUID player) {
        hitMap.remove(player);
    }

    public void scheduleUnregisterNextTick(final UUID player) {
        unregister(player);
        hitMap.put(player, 3);
    }

    @Listener(order = Order.DEFAULT)
    public void onHit(final DamageEntityEvent event) {
        final Optional<Player> optionalPlayer =
                event.getCause().get(DamageEntityEvent.CREATOR, UUID.class)
                        .flatMap(Sponge.getServer()::getPlayer);
        if (!optionalPlayer.isPresent()) {
            return;
        }
        final Player vengelis = optionalPlayer.get();
        final Optional<ManagedMatch> optionalManagedMatch =
                matchManager.getMatchOf(vengelis.getUniqueId());
        if (!optionalManagedMatch.isPresent()) {
            return;
        }
        if (hitMap.containsKey(vengelis.getUniqueId())) {
            return;
        }

        final int hitCount = hitMap.get(vengelis.getUniqueId());
        hitMap.replace(vengelis.getUniqueId(), hitCount == 4 ? 0 : hitCount + 1);
        resetCooldown(vengelis.getUniqueId()); //Reset hit "cooldown"
        if (hitCount != 4) {
            return;
        }
        final ManagedMatch managedMatch = optionalManagedMatch.get();
        //Get all knavises in the current match
        final Collection<Player> knavises =
                managedMatch.getGameEngine().getPlayersOfChallenger(knavis);
        //Give them fury for 1second
        for (final Player knavis : knavises) {
            final PotionEffectData data = knavis.get(PotionEffectData.class).orElseThrow(
                    () -> new IllegalArgumentException("Unable to get potion effect data!"));
            // FIXME
            data.addElement(null /*(PotionEffect) new BuffEffectFury(1, 0)*/);  //1 Second of fury to all knavises | Safe cast because of mixins.
            knavis.offer(data);
        }
    }

    @Override
    public void tick() {
        super.tick();
        final PotionEffect potionEffect = PotionEffect.of(PotionEffectTypes.STRENGTH, 2, 1);
        for (final Map.Entry<UUID, Integer> entry : hitMap.entrySet()) {
            final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(entry.getKey());
            if (!optionalPlayer.isPresent()) {
                scheduleUnregisterNextTick(entry.getKey());
                continue;
            }
            final Player player = optionalPlayer.get();
            final PotionEffectData potionEffectData = player.get(PotionEffectData.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get potion data!"));
            switch (entry.getValue()) {
                case 3:
                    player.offer(potionEffectData.copy().addElement(potionEffect));
                    break;
                case 4:
                    potionEffectData.removeAll(
                            effect -> effect.getType() == PotionEffectTypes.STRENGTH); //Remove strength
                    player.offer(potionEffectData);
                default:
                    break;
            }
        }
    }
}
