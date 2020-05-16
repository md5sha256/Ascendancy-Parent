package com.gmail.andrewandy.ascendency.serverplugin.game.challenger;

import am2.buffs.BuffEffectEntangled;
import com.gmail.andrewandy.ascendency.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendency.lib.game.data.game.ChallengerDataImpl;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.AbstractAbility;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.AbstractCooldownAbility;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendency.serverplugin.game.util.MathUtils;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.Team;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.util.Common;
import com.gmail.andrewandy.ascendency.serverplugin.util.keybind.ActiveKeyPressedEvent;
import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.data.manipulator.mutable.entity.FallingBlockData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class Breezy extends AbstractChallenger {

    private static final Breezy instance = new Breezy();

    @Inject private static PlayerMatchManager matchManager;

    private Breezy() {
        super("Breezy", new Ability[] {Oops.instance}, new PlayerSpecificRune[0],
            Challengers.getLoreOf("Breezy"));
    }

    public static Breezy getInstance() {
        return instance;
    }

    @Override public IChallengerData toData() {
        try {
            return new ChallengerDataImpl(getName(), new File("Some Path"),
                Challengers.getLoreOf(getName()));
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static class Oops extends AbstractCooldownAbility {

        private static final Oops instance = new Oops();

        public static Oops getInstance() {
            return instance;
        }

        private Oops() {
            super("Oops", true, 6, TimeUnit.SECONDS);
        }

        @Listener(order = Order.LAST)
        public void onActiveKeyPress(final ActiveKeyPressedEvent event) {
            //If not registered or on cooldown.
            if (!isRegistered(event.getPlayer().getUniqueId()) || isOnCooldown(
                event.getPlayer().getUniqueId())) {
                return;
            }
            final Player player = event.getPlayer();
            executeAs(player);
        }

        /**
         * Execute this ability as a given player.
         *
         * @param player The {@link Player} object of to execute as.
         */
        public void executeAs(final Player player) {
            final Predicate<Location<World>> circlePredicate =
                MathUtils.isWithinSphere(player.getLocation(), 6);
            final Optional<Team> optionalTeam = matchManager.getTeamOf(player.getUniqueId());
            if (!optionalTeam.isPresent()) {
                return;
            }
            final Team team = optionalTeam.get();
            final Collection<Player> players = Common
                .getEntities(Player.class, player.getLocation().getExtent(), (Player entity) ->
                    !team.equals(matchManager.getTeamOf(entity.getUniqueId()).orElse(null))
                        && circlePredicate.test(entity.getLocation()));
            final World world = player.getWorld();
            for (final Player p : players) {
                final FallingBlock fallingBlock = (FallingBlock) world
                    .createEntity(EntityTypes.FALLING_BLOCK,
                        p.getLocation().getPosition().add(0, 3f, 0));
                final FallingBlockData fallingBlockData = fallingBlock.getFallingBlockData();
                //Make sure anvil can't be placed (i.e self destructs on place)
                fallingBlockData.set(Keys.CAN_PLACE_AS_BLOCK, false);
                fallingBlock.offer(fallingBlockData);
                fallingBlock.setCreator(player.getUniqueId());
                fallingBlock.fallDamagePerBlock().set(100D);
                fallingBlock.maxFallDamage()
                    .set(calculateDamageToDeal(player)); //Override damage per block
                final PotionEffectData potionEffectData = p.get(PotionEffectData.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get potion data!"));
                potionEffectData.addElement((PotionEffect) new BuffEffectEntangled(1,
                    0)); //Entanglement 1 | Safe cast due to sponge mixins.
                p.offer(potionEffectData);
            }
            resetCooldown(player.getUniqueId()); //Reset the cooldown ticker to 0.
        }

        private double calculateDamageToDeal(final Player player) {
            final int abilityPower = Common.getAbilityPower(player);
            final int level = Math.floorDiv(abilityPower, 10);
            return level >= 6 ? 54 : level * 9;
        }

        @Override public void tick() {
            //Tick the cooldowns
            super.tick();
            //Update perma speed 2
            for (final UUID uuid : registered) {
                Sponge.getServer().getPlayer(uuid).ifPresent((Player player) -> {
                    final PotionEffectData data = player.get(PotionEffectData.class).orElseThrow(
                        () -> new IllegalStateException("Unable to get potion effect data!"));
                    data.addElement(PotionEffect.of(PotionEffectTypes.SPEED, 1, 1)); //Speed 2
                    player.offer(data);
                });
            }
        }
    }

    public static class RuneBoom extends AbstractAbility {

        private RuneBoom() {
            super("RuneBoom", false);
        }
        

    }
}
