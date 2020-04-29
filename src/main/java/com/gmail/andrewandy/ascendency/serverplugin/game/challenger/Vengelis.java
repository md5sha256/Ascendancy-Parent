package com.gmail.andrewandy.ascendency.serverplugin.game.challenger;

import am2.api.event.SpellCastEvent;
import am2.buffs.BuffEffectEntangled;
import com.gmail.andrewandy.ascendency.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendency.lib.game.data.game.ChallengerDataImpl;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.AbstractAbility;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendency.serverplugin.game.util.MathUtils;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.util.Common;
import com.gmail.andrewandy.ascendency.serverplugin.util.game.Tickable;
import com.gmail.andrewandy.ascendency.serverplugin.util.keybind.ActiveKeyPressedEvent;
import com.google.inject.Inject;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class Vengelis extends AbstractChallenger {

    private static final Vengelis instance = new Vengelis();

    @Inject private static PlayerMatchManager matchManager;

    private Vengelis() {
        super("Vengelis", new Ability[] {Gyration.instance}, new PlayerSpecificRune[0],
            Challengers.getLoreOf("Vengelis"));
    }

    public static Vengelis getInstance() {
        return instance;
    }

    @Override public IChallengerData toData() {
        try {
            return new ChallengerDataImpl(getName(), new File("Some path"), getLore());
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }


    public static class Gyration extends AbstractAbility implements Tickable {

        public static final Gyration instance = new Gyration();

        private final UUID uuid = UUID.randomUUID();
        private final Map<UUID, Long> cooldownMap = new HashMap<>();
        private final Collection<UUID> registered = new HashSet<>(), active = new HashSet<>();

        private Gyration() {
            super("Gyration", true);
        }

        @Listener(order = Order.LATE) public void onActivePressed(final ActiveKeyPressedEvent event) {
            final Player player = event.getPlayer();
            if (!isActiveOnPlayer(player.getUniqueId()) && registered
                .contains(player.getUniqueId())) {
                active.add(player.getUniqueId());
            }
        }

        @SubscribeEvent public void onSpellCast(final SpellCastEvent event) {
            final EntityLivingBase caster = event.entityLiving;
            if (!canExecuteRoot(caster.getPersistentID())) {
                return;
            }
            Sponge.getServer().getPlayer(caster.getPersistentID()).ifPresent(this::executeAsPlayer);
        }

        @Listener public void onAttack(final DamageEntityEvent event) {
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
            return !cooldownMap.containsKey(player) && isActiveOnPlayer(player);
        }

        private boolean isActiveOnPlayer(final UUID player) {
            return active.contains(player);
        }

        public void registerPlayer(final UUID player) {
            unregisterPlayer(player);
            registered.add(player);
        }

        public void unregisterPlayer(final UUID player) {
            registered.remove(player);
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
            final PotionEffect effect = (PotionEffect) new BuffEffectEntangled(1,
                0); //Entanglement 1 | Raw cast is fine because of sponge mixins
            for (final Player nearby : nearbyPlayers) {
                final PotionEffectData data = nearby.get(PotionEffectData.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get potion data!"));
                data.addElement(effect);
                nearby.offer(data);
            }
            cooldownMap.put(player.getUniqueId(), 0L);
        }

        @Override public UUID getUniqueID() {
            return uuid;
        }

        @Override public void tick() {
            cooldownMap.entrySet()
                .removeIf(ChallengerUtils.mapTickPredicate(10, TimeUnit.SECONDS, null));
        }
    }
}
