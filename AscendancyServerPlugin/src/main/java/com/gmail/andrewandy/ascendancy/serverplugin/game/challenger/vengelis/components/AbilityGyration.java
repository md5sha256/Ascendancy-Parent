package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.vengelis.components;

import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractCooldownAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.game.util.MathUtils;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.gmail.andrewandy.ascendancy.serverplugin.util.keybind.ActiveKeyPressedEvent;
import com.gmail.andrewandy.ascendancy.serverplugin.util.keybind.ActiveKeyReleasedEvent;
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
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class AbilityGyration extends AbstractCooldownAbility {

    private final PlayerMatchManager matchManager;

    private final Collection<UUID> active = new HashSet<>();
    //Represents whether they have an active 1st hit - do not remove!

    @AssistedInject
    AbilityGyration(@Assisted final Challenger toBind,
                    final PlayerMatchManager matchManager) {
        super("Gyration", true, 10, TimeUnit.SECONDS, toBind);
        this.matchManager = matchManager;
    }

    @Listener(order = Order.LAST)
    public void onActivePressed(final ActiveKeyPressedEvent event) {
        final Player player = event.getPlayer();
        if (!isActiveOnPlayer(player.getUniqueId()) && !isRegistered(player.getUniqueId())) {
            active.add(player.getUniqueId());
        }
    }

    @Listener(order = Order.LAST)
    public void onActiveKeyRelease(final ActiveKeyReleasedEvent event) {
        active.remove(event.getPlayer().getUniqueId());
    }

    @Listener(order = Order.LAST)
    public void onAttack(final DamageEntityEvent event) {
        final Optional<Player> source = event.getCause().get(DamageEntityEvent.CREATOR, UUID.class)
                .flatMap(Sponge.getServer()::getPlayer);
        if (!source.isPresent()) {
            return;
        }
        final Player player = source.get();
        if (canExecuteRoot(player.getUniqueId())) {
            executeAsPlayer(player);
        }
    }

    private boolean canExecuteRoot(final UUID player) {
        return !isOnCooldown(player) && isActiveOnPlayer(player);
    }

    private boolean isActiveOnPlayer(final UUID player) {
        return active.contains(player);
    }

    private void executeAsPlayer(final Player player) {
        active.remove(player.getUniqueId());
        final PotionEffectData playerPEData = player.get(PotionEffectData.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get potion data!"));
        playerPEData
                .addElement(PotionEffect.of(PotionEffectTypes.SPEED, 0, 1)); //Speed 1 for 1 second.
        player.offer(playerPEData);
        final Predicate<Location<World>> sphereCheck =
                MathUtils.isWithinSphere(player.getLocation(), 6);
        final Team team = matchManager.getTeamOf(player.getUniqueId()).orElse(null);
        final Predicate<Player> predicate =
                (Player target) -> team != matchManager.getTeamOf(target.getUniqueId()).orElse(null)
                        && sphereCheck.test(player.getLocation());
        final Collection<Player> nearbyPlayers =
                Common.getEntities(Player.class, player.getLocation().getExtent(), predicate);
        //FIXME
        final PotionEffect effect = null; /*(PotionEffect) new BuffEffectEntangled(1, 0); //Entanglement 1 | Raw cast is fine because of sponge mixins */
        for (final Player nearby : nearbyPlayers) {
            final PotionEffectData data = nearby.get(PotionEffectData.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get potion data!"));
            data.addElement(effect);
            nearby.offer(data);
        }
        cooldownMap.put(player.getUniqueId(), 0L);
    }

    @Override
    public void tick() {
        super.tick();
    }
}
