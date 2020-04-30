package com.gmail.andrewandy.ascendency.serverplugin.game.challenger;

import am2.buffs.BuffEffectManaRegen;
import am2.buffs.BuffEffectSilence;
import com.gmail.andrewandy.ascendency.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.AbstractCooldownAbility;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendency.serverplugin.util.Common;
import com.gmail.andrewandy.ascendency.serverplugin.util.keybind.ActiveKeyHandler;
import com.gmail.andrewandy.ascendency.serverplugin.util.keybind.ActiveKeyPressedEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.ChangeEntityPotionEffectEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Solace extends AbstractChallenger {

    private static final Solace instance = new Solace();

    private Solace() {
        super("Solace", new Ability[] {CallbackOfTheAfterlife.instance, UndiminishedSoul.instance},
            new PlayerSpecificRune[0], Challengers.getLoreOf("Solace"));
    }

    public static Solace getInstance() {
        return instance;
    }

    @Override public IChallengerData toData() {
        return null;
    }

    public static class CallbackOfTheAfterlife extends AbstractCooldownAbility {

        public static final CallbackOfTheAfterlife instance = new CallbackOfTheAfterlife();
        private final Map<UUID, Long> soulRegister = new HashMap<>(); //Whoever has the souls
        private final Map<UUID, UUID> soulMap = new HashMap<>(); //Maps Solace to its target.

        private CallbackOfTheAfterlife() {
            super("CallBackOfTheAfterlife", true, 5, TimeUnit.SECONDS);
        }

        public static CallbackOfTheAfterlife getInstance() {
            return instance;
        }

        @Listener(order = Order.LAST)
        public void onActiveKeyPress(final ActiveKeyPressedEvent event) {
            if (ActiveKeyHandler.INSTANCE
                .isKeyPressed(event.getPlayer())) { //If active key was already pressed, skip.
                return;
            }
            if (cooldownMap.containsKey(event.getPlayer().getUniqueId())) { //If on cooldown, skip
                return;
            }
            final Optional<Player> lowestHealth =
                event.getPlayer().getNearbyEntities(10).stream().filter(Player.class::isInstance)
                    .map(Player.class::cast).min((Player player1, Player player2) -> {
                    final double h1 = player1.health().get(), h2 = player2.health().get();
                    return Double.compare(h1, h2);
                });
            if (!lowestHealth.isPresent()) {
                return;
            }
            final Player lowest = lowestHealth.get();
            soulMap.put(event.getPlayer().getUniqueId(),
                lowest.getUniqueId()); //Map the invoker (solace) to the person with the soul.
            soulRegister.put(lowest.getUniqueId(),
                getCooldownDuration()); //Add the target to the registered soul map.
        }


        @Listener(order = Order.LATE) public void onFatalDeath(final DamageEntityEvent event) {
            final Entity target = event.getTargetEntity();
            final Optional<HealthData> data = target.get(HealthData.class);
            assert data.isPresent();
            final HealthData healthData = data.get();
            if (!event.willCauseDeath()) {
                return;
            }
            if (!soulMap.containsValue(event.getTargetEntity().getUniqueId())) {
                return;
            }
            assert target instanceof Player;
            event.setCancelled(true);
            healthData.health().set(20D); //Set health to 20
            target.offer(healthData);
            soulMap.entrySet().removeIf(
                (Map.Entry<UUID, UUID> entry) -> { //Key = Solace, Value = Player with soul.
                    final boolean ret =
                        entry.getValue().equals(event.getTargetEntity().getUniqueId());
                    if (ret) {
                        cooldownMap.put(entry.getKey(), 0L); //Add Solace to cooldown.
                    }
                    return ret;
                }); //Uses the soul
        }

        @Override public void tick() {
            super.tick();
            soulRegister.entrySet()
                .removeIf(ChallengerUtils.mapTickPredicate(getCooldownDuration(), soulMap::remove));
            cooldownMap.entrySet().removeIf(
                ChallengerUtils.mapTickPredicate(Common.toTicks(1, TimeUnit.MINUTES), null));
        }
    }


    public static class UndiminishedSoul extends AbstractCooldownAbility {

        private static final UndiminishedSoul instance = new UndiminishedSoul();


        private UndiminishedSoul() {
            super("Undiminished Soul", false, 5, TimeUnit.SECONDS);
        }

        public static UndiminishedSoul getInstance() {
            return instance;
        }

        @Listener public void onPotionAdded(final ChangeEntityPotionEffectEvent.Gain event) {
            if (!isRegistered(event.getTargetEntity().getUniqueId())) {
                return;
            }
            final Entity entity = event.getTargetEntity();
            if (!(entity instanceof Player)) {
                return;
            }
            final Player player = (Player) entity;
            final PotionEffect effect = event.getPotionEffect();
            if (effect instanceof BuffEffectSilence) { //If silence then remove.
                event.setCancelled(true);
                resetCooldown(player.getUniqueId());
            }
        }

        //TODO
        public void onSpellCast() {

        }

        @Override public void tick() {
            for (final UUID uuid : registered) {
                final Optional<Player> optional = Sponge.getServer().getPlayer(uuid);
                optional.ifPresent((Player player) -> {
                    final PotionEffectData peData = player.get(PotionEffectData.class).orElseThrow(
                        () -> new IllegalStateException("Unable to get potion effect data!"));
                    peData.addElement((PotionEffect) new BuffEffectManaRegen(1,
                        2)); //Mana regen 2 | Safe cast as per sponge mixins.
                    player.offer(peData);
                });
            }
            super.tick();
        }
    }
}
