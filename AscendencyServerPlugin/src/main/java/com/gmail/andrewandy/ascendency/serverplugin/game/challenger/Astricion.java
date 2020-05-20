package com.gmail.andrewandy.ascendency.serverplugin.game.challenger;

import am2.buffs.BuffEffectEntangled;
import com.gmail.andrewandy.ascendency.lib.game.data.IChampionData;
import com.gmail.andrewandy.ascendency.lib.game.data.game.ChampionDataImpl;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.AbstractAbility;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.AbstractRune;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;
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

    private Astricion() {
        super("Astricion",
                new Ability[]{Suppression.instance, DemonicCapacity.instance},
                new PlayerSpecificRune[]{ReleasedLimit.instance, DiabolicResistance.instance, EmpoweringRage.instance},
                Season1Challengers.getLoreOf("Astricion"));
    }

    public static Astricion getInstance() {
        return instance;
    }

    public static class ReleasedLimit extends AbstractRune {
        private static final ReleasedLimit instance = new ReleasedLimit();

        @Override
        public void applyTo(Player player) {

        }

        @Override
        public void clearFrom(Player player) {

        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void tick() {

        }

        @Override
        public int getContentVersion() {
            return 0;
        }

        @Override
        public DataContainer toContainer() {
            return null;
        }
    }

    public static class DiabolicResistance extends AbstractRune {
        public static final DiabolicResistance instance = new DiabolicResistance();

        @Override
        public void applyTo(Player player) {

        }

        @Override
        public void clearFrom(Player player) {

        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void tick() {

        }

        @Override
        public int getContentVersion() {
            return 0;
        }

        @Override
        public DataContainer toContainer() {
            return null;
        }
    }

    public static class EmpoweringRage extends AbstractRune {
        public static final EmpoweringRage instance = new EmpoweringRage();

        @Override
        public void applyTo(Player player) {

        }

        @Override
        public void clearFrom(Player player) {

        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void tick() {

        }

        @Override
        public int getContentVersion() {
            return 0;
        }

        @Override
        public DataContainer toContainer() {
            return null;
        }
    }

    @Override
    public IChampionData toData() {
        try {
            return new ChampionDataImpl(getName(), new File("Path to data"), getLore());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class Suppression extends AbstractAbility {

        private static final Suppression instance = new Suppression();
        private Collection<UUID> active = new HashSet<>();

        private Suppression() {
            super("Suppression", true);
        }

        public static Suppression getInstance() {
            return instance;
        }

        public void activateAs(UUID player) {
            active.remove(player);
            active.add(player);
        }

        //TODO use entangle & give resistance
        @Listener
        public void onEntityDamage(DamageEntityEvent event) {
            Entity entity = event.getTargetEntity();
            if (!(entity instanceof Player) || !active.contains(entity.getUniqueId())) {
                return;
            }
            PotionEffect entanglement = (PotionEffect) (Object) new BuffEffectEntangled(4, 1); //Safe cast as per forge's runtime changes
            event.setBaseDamage(calculateIncomingDamage(event.getBaseDamage())); //Modifies the base damage directly
        }

        public double calculateIncomingDamage(double incoming) {
            return incoming * 0.6D; //Reduced incoming damage.
        }
    }

    private static class DemonicCapacity extends AbstractAbility {
        private static final DemonicCapacity instance = new DemonicCapacity();

        public static DemonicCapacity getInstance() {
            return instance;
        }

        private Collection<UUID> active = new HashSet<>();

        private DemonicCapacity() {
            super("Demonic Capacity", false);
        }

        public void activateAs(UUID player) {
            active.remove(player);
            active.add(player);
        }

        @Listener
        public void onEntityDamage(DamageEntityEvent event) {
            Entity entity = event.getTargetEntity();
            if ((!active.contains(entity.getUniqueId())) || (!(entity instanceof Player))) {
                return;
            } else {
                int astricionHealth = (int) Math.round(((Player) entity).getHealthData().health().get());
                Optional<PotionEffectData> optional = entity.getOrCreate(PotionEffectData.class);
                if (!optional.isPresent()) {
                    throw new IllegalStateException("Potion effect data could not be gathered for " + entity.getUniqueId().toString());
                }

                PotionEffectData data = optional.get();
                PotionEffect[] effects = new PotionEffect[]{PotionEffect.builder()
                        //Strength scaling on current health
                        .potionType(PotionEffectTypes.STRENGTH)
                        .duration(999999).amplifier((int) Math.round((astricionHealth - 10) / 10D)).build()};
                for (PotionEffect effect : effects) {
                    data.addElement(effect);
                }
            }
        }

        @Listener
        public void onPlayerRespawn(RespawnPlayerEvent event) {
            Player player = event.getTargetEntity();
            if (!active.contains(player.getUniqueId())) {
                return;
            }
            int astricionHealth = (int) Math.round(player.getHealthData().maxHealth().get());
            Optional<PotionEffectData> optional = player.getOrCreate(PotionEffectData.class);
            if (!optional.isPresent()) {
                throw new IllegalStateException("Potion effect data could not be gathered for " + player.getUniqueId().toString());
            }

            PotionEffectData data = optional.get();
            PotionEffect[] effects = new PotionEffect[]{PotionEffect.builder()
                    //Strength scaling on current health
                    .potionType(PotionEffectTypes.STRENGTH)

                    .duration(999999).amplifier((int) Math.round((astricionHealth - 10) / 10D)).build()};
            for (PotionEffect effect : effects) {
                data.addElement(effect);
            }
        }

    }
}
