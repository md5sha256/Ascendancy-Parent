package com.gmail.andrewandy.ascendency.serverplugin.game.challenger;

import am2.api.extensions.IEntityExtension;
import am2.extensions.EntityExtension;
import com.gmail.andrewandy.ascendency.lib.game.data.IChampionData;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.AbstractAbility;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendency.serverplugin.util.Common;
import com.gmail.andrewandy.ascendency.serverplugin.util.game.Tickable;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DamageEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Solace extends AbstractChallenger {

    private static final Solace instance = new Solace();

    private Solace() {
        super("Solace",
                new Ability[]{CallbackOfTheAfterlife.instance},
                new PlayerSpecificRune[0],
                Challengers.getLoreOf("Solace"));
    }

    public static Solace getInstance() {
        return instance;
    }

    @Override
    public IChampionData toData() {
        return null;
    }

    public static class CallbackOfTheAfterlife extends AbstractAbility implements Tickable {

        public static final CallbackOfTheAfterlife instance = new CallbackOfTheAfterlife();
        private static final long tickCount = Common.toTicks(5, TimeUnit.SECONDS);
        private final UUID uuid = UUID.randomUUID();
        private Map<UUID, Long> registered = new HashMap<>(); //Whoever has the souls
        private Map<UUID, UUID> soulMap = new HashMap<>(); //Maps Solace to its target.

        private CallbackOfTheAfterlife() {
            super("CallBackOfTheAfterlife", true);
        }

        public static CallbackOfTheAfterlife getInstance() {
            return instance;
        }

        @Override
        public UUID getUniqueID() {
            return uuid;
        }

        public void activateAs(UUID uuid) {
            Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(uuid);
            if (!optionalPlayer.isPresent()) {
                throw new IllegalArgumentException("Player does not exist!");
            }
            Player player = optionalPlayer.get();
            IEntityExtension extension = EntityExtension.For((EntityLivingBase) player);
            extension.setCurrentMana(extension.getCurrentMana() - 10f);
        }

        @Listener(order = Order.LATE)
        public void onFatalDeath(DamageEntityEvent event) {
            Entity target = event.getTargetEntity();
            double damage = event.getFinalDamage();
            Optional<HealthData> data = target.get(HealthData.class);
            assert data.isPresent();
            HealthData healthData = data.get();
            if (damage <= healthData.health().get()) {
                return;
            }
            if (!soulMap.containsValue(event.getTargetEntity().getUniqueId())) {
                return;
            }
            assert target instanceof Player;
            event.setCancelled(true);
            healthData.health().set(20D); //Set health to 20
            target.offer(healthData);
            soulMap.entrySet().removeIf(entry -> entry.getValue().equals(event.getTargetEntity().getUniqueId())); //Use the soul

        }

        @Override
        public void tick() {
            registered.entrySet().removeIf(ChallengerUtils.mapTickPredicate(tickCount, soulMap::remove));
        }
    }
}
