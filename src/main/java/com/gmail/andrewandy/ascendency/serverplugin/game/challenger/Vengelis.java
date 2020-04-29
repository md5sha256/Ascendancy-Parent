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

    @Inject private static PlayerMatchManager matchManager;

    private Vengelis() {
        super("Vengelis", new Ability[0], new PlayerSpecificRune[0],
            Challengers.getLoreOf("Vengelis"));
    }

    public static class Gyration extends AbstractAbility implements Tickable {

        private Map<UUID, Long> cooldownMap = new HashMap<>();
        private Collection<UUID> registered = new HashSet<>(), active = new HashSet<>();

        private Gyration() {
            super("Gyration", true);
        }

        @Listener(order = Order.LATE) public void onActivePressed(ActiveKeyPressedEvent event) {
            Player player = event.getPlayer();
            if (!isActiveOnPlayer(player.getUniqueId()) && registered
                .contains(player.getUniqueId())) {
                active.add(player.getUniqueId());
            }
        }

        @SubscribeEvent public void onSpellCast(SpellCastEvent event) {
            EntityLivingBase entityLivingBase = event.entityLiving;
            if (!canExecuteRoot(entityLivingBase.getPersistentID())) {
                return;
            }
            Sponge.getServer().getPlayer(entityLivingBase.getPersistentID())
                .ifPresent(this::executeAsPlayer);
        }

        @Listener public void onAttack(DamageEntityEvent event) {
            Optional<Player> source = event.getCause().get(DamageEntityEvent.SOURCE, Player.class);
            if (!source.isPresent()) {
                return;
            }
            Player player = source.get();
            if (canExecuteRoot(player.getUniqueId())) {
                executeAsPlayer(player);
            }
        }

        private boolean canExecuteRoot(UUID player) {
            return !cooldownMap.containsKey(player) && isActiveOnPlayer(player);
        }

        private boolean isActiveOnPlayer(UUID player) {
            return active.contains(player);
        }

        public void registerPlayer(UUID player) {
            unregisterPlayer(player);
            registered.add(player);
        }

        public void unregisterPlayer(UUID player) {
            registered.remove(player);
        }


        private void executeAsPlayer(Player player) {
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

        @Override public void tick() {
            cooldownMap.entrySet()
                .removeIf(ChallengerUtils.mapTickPredicate(10, TimeUnit.SECONDS, null));
        }
    }


    @Override public IChallengerData toData() {
        try {
            return new ChallengerDataImpl(getName(), new File("Some path"), getLore());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
