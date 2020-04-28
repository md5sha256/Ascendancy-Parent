package com.gmail.andrewandy.ascendency.serverplugin.game.challenger;

import am2.buffs.BuffEffectManaRegen;
import am2.buffs.BuffEffectSilence;
import com.gmail.andrewandy.ascendency.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.AbstractAbility;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendency.serverplugin.util.Common;
import com.gmail.andrewandy.ascendency.serverplugin.util.game.Tickable;
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

import java.util.*;
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

    public static class CallbackOfTheAfterlife extends AbstractAbility implements Tickable {

        public static final CallbackOfTheAfterlife instance = new CallbackOfTheAfterlife();
        private static final long tickCount = Common.toTicks(5, TimeUnit.SECONDS);
        private final UUID uuid = UUID.randomUUID();
        private Map<UUID, Long> registered = new HashMap<>(); //Whoever has the souls
        private Map<UUID, Long> cooldownMap = new HashMap<>();
        private Map<UUID, UUID> soulMap = new HashMap<>(); //Maps Solace to its target.

        private CallbackOfTheAfterlife() {
            super("CallBackOfTheAfterlife", true);
        }

        public static CallbackOfTheAfterlife getInstance() {
            return instance;
        }

        @Override public UUID getUniqueID() {
            return uuid;
        }

        @Listener public void onActiveKeyPress(ActiveKeyPressedEvent event) {
            if (ActiveKeyHandler.INSTANCE
                .isKeyPressed(event.getPlayer())) { //If active key was already pressed, skip.
                return;
            }
            if (cooldownMap.containsKey(event.getPlayer().getUniqueId())) { //If on cooldown, skip
                return;
            }
            Optional<Player> lowestHealth = event.getPlayer().getNearbyEntities(10).stream()
                .filter(entity -> entity instanceof Player).map(entity -> (Player) entity)
                .min((Player player1, Player player2) -> {
                    double h1 = player1.health().get(), h2 = player2.health().get();
                    return Double.compare(h1, h2);
                });
            if (!lowestHealth.isPresent()) {
                return;
            }
            Player lowest = lowestHealth.get();
            soulMap.put(event.getPlayer().getUniqueId(),
                lowest.getUniqueId()); //Map the invoker (solace) to the person with the soul.
            registered
                .put(lowest.getUniqueId(), tickCount); //Add the target to the registered soul map.
        }


        @Listener(order = Order.LATE) public void onFatalDeath(DamageEntityEvent event) {
            Entity target = event.getTargetEntity();
            Optional<HealthData> data = target.get(HealthData.class);
            assert data.isPresent();
            HealthData healthData = data.get();
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
                    boolean ret = entry.getValue().equals(event.getTargetEntity().getUniqueId());
                    if (ret) {
                        cooldownMap.put(entry.getKey(), 0L); //Add Solace to cooldown.
                    }
                    return ret;
                }); //Uses the soul
        }

        @Override public void tick() {
            registered.entrySet()
                .removeIf(ChallengerUtils.mapTickPredicate(tickCount, soulMap::remove));
            cooldownMap.entrySet().removeIf(
                ChallengerUtils.mapTickPredicate(Common.toTicks(1, TimeUnit.MINUTES), null));
        }
    }


    public static class UndiminishedSoul extends AbstractAbility implements Tickable {

        private static final UndiminishedSoul instance = new UndiminishedSoul();
        private final UUID uniqueID = UUID.randomUUID();
        private Collection<UUID> active = new HashSet<>();
        private Map<UUID, Long> dispelCooldown = new HashMap<>();


        private UndiminishedSoul() {
            super("Undiminished Soul", false);
        }

        public static UndiminishedSoul getInstance() {
            return instance;
        }

        public void register(UUID uuid) {
            deregister(uuid);
            active.add(uuid);
        }

        public void deregister(UUID uuid) {
            active.remove(uuid);
            dispelCooldown.remove(uuid);
        }

        @Listener public void onPotionAdded(ChangeEntityPotionEffectEvent.Gain event) {
            if (!active.contains(event.getTargetEntity().getUniqueId())) {
                return;
            }
            Entity entity = event.getTargetEntity();
            if (!(entity instanceof Player)) {
                return;
            }
            Player player = (Player) entity;
            PotionEffect effect = event.getPotionEffect();
            if (effect instanceof BuffEffectSilence) { //If silence then remove.
                event.setCancelled(true);
                dispelCooldown.put(player.getUniqueId(), 0L);
            }
        }

        //TODO
        public void onSpellCast() {

        }


        @Override public UUID getUniqueID() {
            return uniqueID;
        }

        @Override public void tick() {
            for (UUID uuid : active) {
                Optional<Player> optional = Sponge.getServer().getPlayer(uuid);
                optional.ifPresent((Player player) -> {
                    PotionEffectData peData = player.get(PotionEffectData.class).orElseThrow(
                        () -> new IllegalStateException("Unable to get potion effect data!"));
                    peData.addElement((PotionEffect) new BuffEffectManaRegen(1,
                        2)); //Mana regen 2 | Safe cast as per sponge mixins.
                    player.offer(peData);
                });
            }
            dispelCooldown.entrySet()
                .removeIf(ChallengerUtils.mapTickPredicate(5, TimeUnit.SECONDS, null));
        }
    }
}
