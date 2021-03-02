package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.components;

import com.gmail.andrewandy.ascendancy.lib.util.MutablePair;
import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractTickableAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
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

public class AbilityReleasedRebellion extends AbstractTickableAbility {

    private final Map<UUID, MutablePair<UUID, Long>> tickMap = new HashMap<>();

    private AbilityReleasedRebellion(@NotNull final Challenger challenger) {
        super("Released Rebellion", false, challenger);
    }


    @Listener
    public void onPlayerAttack(final DamageEntityEvent event) {
        final Entity target = event.getTargetEntity();
        final Optional<Player> optionalPlayer =
                event.getCause().get(DamageEntityEvent.CREATOR, UUID.class)
                        .flatMap(Sponge.getServer()::getPlayer);
        if (!optionalPlayer.isPresent()) {
            return;
        }
        final Player player = optionalPlayer.get();
        if (!isRegistered(player.getUniqueId())) {
            return;
        }
        tickMap
                .compute(player.getUniqueId(), ((uuid, pair) -> new MutablePair<>(target.getUniqueId(), 0L)));
    }

    @Override
    public void tick() {
        final long ticks = Common.toTicks(3, TimeUnit.SECONDS);
        tickMap.entrySet().removeIf(entry -> {
            final MutablePair<UUID, Long> pair = entry.getValue();
            final long val = pair.getValue() + 1;
            pair.setValue(pair.getValue() + 1);
            return val >= ticks;
        });
    }

    @Listener(order = Order.LAST)
    public void onProc(final AbilityCircletOfTheAccused.ProcEvent event) { //Handles the proc event
        final Player invoker = event.getInvoker();
        if (!tickMap.containsKey(invoker.getUniqueId())) {
            return;
        }
        final Optional<Player> optionalPlayer =
                Sponge.getServer().getPlayer(tickMap.get(invoker.getUniqueId()).getKey());
        optionalPlayer.ifPresent(event::setTarget);
    }

}
