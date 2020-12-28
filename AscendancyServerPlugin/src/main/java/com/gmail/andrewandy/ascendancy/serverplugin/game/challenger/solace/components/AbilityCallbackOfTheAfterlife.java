package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.solace.components;

import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractCooldownAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.gmail.andrewandy.ascendancy.serverplugin.util.keybind.ActiveKeyPressedEvent;
import com.gmail.andrewandy.ascendancy.serverplugin.util.keybind.KeyBindHandler;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
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

public class AbilityCallbackOfTheAfterlife extends AbstractCooldownAbility {

    private final KeyBindHandler keyBindHandler;
    private final Map<UUID, Long> soulRegister = new HashMap<>(); //Whoever has the souls
    private final Map<UUID, UUID> soulMap = new HashMap<>(); //Maps Solace to its target.

    @AssistedInject
    AbilityCallbackOfTheAfterlife(@Assisted final Challenger toBind,
                                  final KeyBindHandler keyBindHandler) {
        super("CallBackOfTheAfterlife", true, 5, TimeUnit.SECONDS, toBind);
        this.keyBindHandler = keyBindHandler;
    }

    @Listener(order = Order.LAST)
    public void onActiveKeyPress(final ActiveKeyPressedEvent event) {
        if (keyBindHandler
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


    @Listener(order = Order.LATE)
    public void onFatalDeath(final DamageEntityEvent event) {
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
        soulMap.entrySet()
                .removeIf((Map.Entry<UUID, UUID> entry) -> { //Key = Solace, Value = Player with soul.
                    final boolean ret = entry.getValue().equals(event.getTargetEntity().getUniqueId());
                    if (ret) {
                        cooldownMap.put(entry.getKey(), 0L); //Add Solace to cooldown.
                    }
                    return ret;
                }); //Uses the soul
    }

    @Override
    public void tick() {
        super.tick();
        soulRegister.entrySet()
                .removeIf(ChallengerUtils.mapTickPredicate(getCooldownDuration(), soulMap::remove));
        cooldownMap.entrySet()
                .removeIf(ChallengerUtils.mapTickPredicate(Common.toTicks(1, TimeUnit.MINUTES), null));
    }
}
