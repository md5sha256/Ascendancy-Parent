package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.astricion.components;


import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractTickableAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;

import java.util.Optional;
import java.util.UUID;

public final class AbilityDemonicCapacity extends AbstractTickableAbility {


    @AssistedInject
    AbilityDemonicCapacity(@Assisted final Challenger bound) {
        super("Demonic Capacity", false, bound);
    }


    @Listener
    public void onEntityDamage(final DamageEntityEvent event) {
        final Entity entity = event.getTargetEntity();
        if ((!isRegistered(entity.getUniqueId())) || (!(entity instanceof Player))) {
            return;
        }
        final int astricionHealth =
                (int) Math.round(((Player) entity).getHealthData().health().get());
        final PotionEffectData data = entity.getOrCreate(PotionEffectData.class).orElseThrow(
                () -> new IllegalStateException(
                        "Potion effect data could not be gathered for " + entity.getUniqueId().toString()));
        final PotionEffect[] effects = new PotionEffect[]{PotionEffect.builder()
                //Strength scaling on current health
                .potionType(PotionEffectTypes.STRENGTH).duration(999999)
                .amplifier((int) Math.round((astricionHealth - 10) / 10D)).build()};
        for (final PotionEffect effect : effects) {
            data.addElement(effect);
        }
    }

    @Listener
    public void onPlayerRespawn(final RespawnPlayerEvent event) {
        final Player player = event.getTargetEntity();
        if (!isRegistered(player.getUniqueId())) {
            return;
        }
        final int astricionHealth = (int) Math.round(player.getHealthData().maxHealth().get());
        final Optional<PotionEffectData> optional = player.getOrCreate(PotionEffectData.class);
        if (!optional.isPresent()) {
            throw new IllegalStateException(
                    "Potion effect data could not be gathered for " + player.getUniqueId().toString());
        }

        final PotionEffectData data = optional.get();
        final PotionEffect[] effects = new PotionEffect[]{PotionEffect.builder()
                //Strength scaling on current health
                .potionType(PotionEffectTypes.STRENGTH)

                .duration(999999).amplifier((int) Math.round((astricionHealth - 10) / 10D)).build()};
        for (final PotionEffect effect : effects) {
            data.addElement(effect);
        }
    }

    @Override
    public void tick() {
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
