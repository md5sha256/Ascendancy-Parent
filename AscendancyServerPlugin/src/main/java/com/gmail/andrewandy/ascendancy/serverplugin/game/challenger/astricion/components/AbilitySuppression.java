package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.astricion.components;

import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.AscendancyAttribute;
import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.AttributeData;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.astricion.Astricion;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.engine.GamePlayer;
import com.gmail.andrewandy.ascendancy.serverplugin.util.keybind.ActiveKeyPressedEvent;
import com.gmail.andrewandy.ascendancy.serverplugin.util.keybind.ActiveKeyReleasedEvent;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DamageEntityEvent;

import java.util.Optional;
import java.util.UUID;

public final class AbilitySuppression extends AbstractAbility {

    private final PlayerMatchManager matchManager;

    @AssistedInject
    public AbilitySuppression(
            @Assisted final Challenger bound,
            final PlayerMatchManager matchManager
    ) {

        super("Suppression", true, bound);
        this.matchManager = matchManager;
    }

    public void activateAs(final UUID player) {
        unregister(player);
        register(player);
    }

    @Override
    public void register(UUID player) {
        super.register(player);
        Sponge.getServer().getPlayer(player).ifPresent(onlinePlayer -> {
            final AttributeData data = onlinePlayer.get(AttributeData.class)
                    .orElseThrow(() -> new IllegalStateException("Failed to get AttributeData for: " + onlinePlayer.getName()));
            final MutableBoundedValue<Integer> damageReduction = data.getAttribute(AscendancyAttribute.DAMAGE_REDUCTION);
            damageReduction.set(damageReduction.get() + 40);
        });
    }

    @Override
    public void unregister(UUID player) {
        super.unregister(player);
        Sponge.getServer().getPlayer(player).ifPresent(onlinePlayer -> {
            final AttributeData data = onlinePlayer.get(AttributeData.class)
                    .orElseThrow(() -> new IllegalStateException("Failed to get AttributeData for: " + onlinePlayer.getName()));
            final MutableBoundedValue<Integer> damageReduction = data.getAttribute(AscendancyAttribute.DAMAGE_REDUCTION);
            damageReduction.set(damageReduction.get() - 40);
        });
    }

    @Listener
    public void onEntityDamage(final DamageEntityEvent event) {
        final Entity entity = event.getTargetEntity();
        if (!(entity instanceof Player) || !isRegistered(entity.getUniqueId())) {
            return;
        }

    }

    @Listener
    public void onActiveKeyPress(final ActiveKeyPressedEvent event) {
        final Optional<ManagedMatch> match =
                matchManager.getMatchOf(event.getPlayer().getUniqueId());
        match.ifPresent((managedMatch -> {
            final Optional<? extends GamePlayer> optionalGamePlayer =
                    managedMatch.getGamePlayerOf(event.getPlayer().getUniqueId());
            if (!optionalGamePlayer.isPresent()) {
                return;
            }
            final GamePlayer gamePlayer = optionalGamePlayer.get();
            final Challenger challenger = gamePlayer.getChallenger();
            if (challenger instanceof Astricion) {
                activateAs(gamePlayer.getPlayerUUID());
            }
        }));
    }

    @Listener
    public void onActiveKeyRelease(final ActiveKeyReleasedEvent event) {
        final Optional<ManagedMatch> match =
                matchManager.getMatchOf(event.getPlayer().getUniqueId());
        match.ifPresent((managedMatch -> {
            final Optional<? extends GamePlayer> optionalGamePlayer =
                    managedMatch.getGamePlayerOf(event.getPlayer().getUniqueId());
            if (!optionalGamePlayer.isPresent()) {
                return;
            }
            final GamePlayer gamePlayer = optionalGamePlayer.get();
            final Challenger challenger = gamePlayer.getChallenger();
            if (challenger instanceof Astricion) {
                unregister(gamePlayer.getPlayerUUID());
            }
        }));
    }

}
