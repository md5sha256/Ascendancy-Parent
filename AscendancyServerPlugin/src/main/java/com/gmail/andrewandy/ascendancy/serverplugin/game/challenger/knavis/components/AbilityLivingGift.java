package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.components;

import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.event.AscendancyServerEvent;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.DamageEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AbilityLivingGift extends AbstractAbility {

    private final Map<UUID, Integer> hitHistory = new HashMap<>();

    @AssistedInject
    AbilityLivingGift(@Assisted final Challenger toBind) {
        super("LivingGift", false, toBind);
    }

    @Override
    public String getName() {
        return "LivingGift";
    }

    @Listener
    public void onDamage(final DamageEntityEvent event) {
        final Optional<Player> optionalPlayer =
                event.getCause().get(DamageEntityEvent.CREATOR, UUID.class)
                        .flatMap(Sponge.getServer()::getPlayer);
        if (!optionalPlayer.isPresent()) {
            return;
        }
        final Player player = optionalPlayer.get();
        if (!hitHistory.containsKey(player.getUniqueId())) {
            return;
        }
        int hits = hitHistory.get(player.getUniqueId());
        if (hits++ == 3) {
            final HealthData data = player.getHealthData();
            data.set(
                    data.health().transform((Double val) -> val + 3.0)); //Add 3 health or 1.5 hearts.
            player.offer(data); //Update the player object.
            hits = 0;
            new LivingGiftUseEvent(player).callEvent();
        }
        hitHistory.replace(player.getUniqueId(), hits); //Update hit count
    }

    public static class LivingGiftUseEvent extends AscendancyServerEvent {

        private final Cause cause;

        public LivingGiftUseEvent(final Player player) {
            this.cause = Cause.builder().named("Player", player).build();
        }

        @Override
        @NotNull
        public Cause getCause() {
            return cause;
        }

    }

}
