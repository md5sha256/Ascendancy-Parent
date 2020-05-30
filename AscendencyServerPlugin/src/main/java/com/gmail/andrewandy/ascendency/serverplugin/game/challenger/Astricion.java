package com.gmail.andrewandy.ascendency.serverplugin.game.challenger;

import am2.buffs.BuffEffectEntangled;
import com.gmail.andrewandy.ascendency.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendency.lib.game.data.game.ChallengerDataImpl;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.AbstractAbility;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.AbstractTickableAbility;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.AbstractRune;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.ManagedMatch;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.SimplePlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.engine.GamePlayer;
import com.gmail.andrewandy.ascendency.serverplugin.util.keybind.ActiveKeyPressedEvent;
import com.gmail.andrewandy.ascendency.serverplugin.util.keybind.ActiveKeyReleasedEvent;
import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class Astricion extends AbstractChallenger {

    private static final Astricion instance = new Astricion();
    @Inject private static PlayerMatchManager matchManager;

    private Astricion() {
        super("Astricion", new Ability[] {Suppression.instance, DemonicCapacity.instance},
            new PlayerSpecificRune[] {ReleasedLimit.instance, DiabolicResistance.instance,
                EmpoweringRage.instance}, Challengers.getLoreOf("Astricion"));
    }

    public static Astricion getInstance() {
        return instance;
    }

    @Override public IChallengerData toData() {
        try {
            return new ChallengerDataImpl(getName(), new File("Path to data"), getLore());
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class ReleasedLimit extends AbstractRune {
        private static final ReleasedLimit instance = new ReleasedLimit();

        @Override public void applyTo(final Player player) {
        }

        @Override public void clearFrom(final Player player) {
        }

        @Override public String getName() {
            return null;
        }

        @Override public void tick() {
        }

        @Override public int getContentVersion() {
            return 0;
        }

        @Override public DataContainer toContainer() {
            return null;
        }
    }


    public static class DiabolicResistance extends AbstractRune {
        public static final DiabolicResistance instance = new DiabolicResistance();

        @Override public void applyTo(final Player player) {

        }

        @Override public void clearFrom(final Player player) {
        }

        @Override public String getName() {
            return null;
        }

        @Override public void tick() {

        }

        @Override public int getContentVersion() {
            return 0;
        }

        @Override public DataContainer toContainer() {
            return null;
        }
    }


    public static class EmpoweringRage extends AbstractRune {
        public static final EmpoweringRage instance = new EmpoweringRage();

        @Override public void applyTo(final Player player) {

        }

        @Override public void clearFrom(final Player player) {

        }

        @Override public String getName() {
            return null;
        }

        @Override public void tick() {

        }

        @Override public int getContentVersion() {
            return 0;
        }

        @Override public DataContainer toContainer() {
            return null;
        }
    }


    private static class Suppression extends AbstractAbility {

        private static final Suppression instance = new Suppression();
        private final Collection<UUID> active = new HashSet<>();

        private Suppression() {
            super("Suppression", true);
        }

        public static Suppression getInstance() {
            return instance;
        }

        public void activateAs(final UUID player) {
            unregister(player);
            register(player);
        }

        //TODO use entangle & give resistance
        @Listener public void onEntityDamage(final DamageEntityEvent event) {
            final Entity entity = event.getTargetEntity();
            if (!(entity instanceof Player) || active.contains(entity.getUniqueId())) {
                return;
            }
            final PotionEffect entanglement = (PotionEffect) (Object) new BuffEffectEntangled(4,
                1); //Safe cast as per forge's runtime changes
            event.setBaseDamage(
                calculateIncomingDamage(event.getBaseDamage())); //Modifies the base damage directly
        }

        public double calculateIncomingDamage(final double incoming) {
            return incoming * 0.6D; //Reduced incoming damage.
        }

        @Listener public void onActiveKeyPress(final ActiveKeyPressedEvent event) {
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
                if (challenger != Astricion.instance) {
                    return;
                }
                activateAs(gamePlayer.getPlayerUUID());
            }));
        }

        @Listener public void onActiveKeyRelease(final ActiveKeyReleasedEvent event) {
            final Optional<ManagedMatch> match =
                SimplePlayerMatchManager.INSTANCE.getMatchOf(event.getPlayer().getUniqueId());
            match.ifPresent((managedMatch -> {
                final Optional<? extends GamePlayer> optionalGamePlayer =
                    managedMatch.getGamePlayerOf(event.getPlayer().getUniqueId());
                if (!optionalGamePlayer.isPresent()) {
                    return;
                }
                final GamePlayer gamePlayer = optionalGamePlayer.get();
                final Challenger challenger = gamePlayer.getChallenger();
                if (challenger != Astricion.instance) {
                    return;
                }
                unregister(gamePlayer.getPlayerUUID());
            }));
        }
    }



    private static class DemonicCapacity extends AbstractTickableAbility {
        private static final DemonicCapacity instance = new DemonicCapacity();

        private DemonicCapacity() {
            super("Demonic Capacity", false);
        }

        public static DemonicCapacity getInstance() {
            return instance;
        }

        @Listener public void onEntityDamage(final DamageEntityEvent event) {
            final Entity entity = event.getTargetEntity();
            if ((!isRegistered(entity.getUniqueId())) || (!(entity instanceof Player))) {
                return;
            }
            final int astricionHealth =
                (int) Math.round(((Player) entity).getHealthData().health().get());
            final Optional<PotionEffectData> optional = entity.getOrCreate(PotionEffectData.class);
            if (!optional.isPresent()) {
                throw new IllegalStateException(
                    "Potion effect data could not be gathered for " + entity.getUniqueId()
                        .toString());
            }

            final PotionEffectData data = optional.get();
            final PotionEffect[] effects = new PotionEffect[] {PotionEffect.builder()
                //Strength scaling on current health
                .potionType(PotionEffectTypes.STRENGTH).duration(999999)
                .amplifier((int) Math.round((astricionHealth - 10) / 10D)).build()};
            for (final PotionEffect effect : effects) {
                data.addElement(effect);
            }
        }

        @Listener public void onPlayerRespawn(final RespawnPlayerEvent event) {
            final Player player = event.getTargetEntity();
            if (!isRegistered(player.getUniqueId())) {
                return;
            }
            final int astricionHealth = (int) Math.round(player.getHealthData().maxHealth().get());
            final Optional<PotionEffectData> optional = player.getOrCreate(PotionEffectData.class);
            if (!optional.isPresent()) {
                throw new IllegalStateException(
                    "Potion effect data could not be gathered for " + player.getUniqueId()
                        .toString());
            }

            final PotionEffectData data = optional.get();
            final PotionEffect[] effects = new PotionEffect[] {PotionEffect.builder()
                //Strength scaling on current health
                .potionType(PotionEffectTypes.STRENGTH)

                .duration(999999)
                .amplifier((int) Math.round((astricionHealth - 10) / 10D)).build()};
            for (final PotionEffect effect : effects) {
                data.addElement(effect);
            }
        }

        @Override public void tick() {
            for (final UUID uuid : registered) {
                final Optional<Player> optionalPlayer = Sponge.getServer().getPlayer(uuid);
                if (!optionalPlayer.isPresent()) {
                    return;
                }
                final Player player = optionalPlayer.get();
                final double health = player.health().get();
                final PotionEffectData peData = player.get(PotionEffectData.class).orElseThrow(
                    () -> new IllegalStateException(
                        "Unable to get potion effect data for " + player.getName()));
                peData.addElement(
                    PotionEffect.builder().potionType(PotionEffectTypes.STRENGTH).duration(1)
                        .amplifier((int) Math.round((health - 10) / 10D)).build());
                player.offer(peData);
            }
        }
    }
}
