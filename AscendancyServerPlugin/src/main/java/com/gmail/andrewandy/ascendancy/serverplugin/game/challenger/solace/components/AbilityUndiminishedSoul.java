package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.solace.components;

import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.AbstractCooldownAbility;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ChangeEntityPotionEffectEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AbilityUndiminishedSoul extends AbstractCooldownAbility {

    @AssistedInject
    AbilityUndiminishedSoul(@Assisted Challenger toBind) {
        super("Undiminished Soul", false, 5, TimeUnit.SECONDS, toBind);
    }

    @Listener
    public void onPotionAdded(final ChangeEntityPotionEffectEvent.Gain event) {
        if (!isRegistered(event.getTargetEntity().getUniqueId())) {
            return;
        }
        final Entity entity = event.getTargetEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        final Player player = (Player) entity;
        final PotionEffect effect = event.getPotionEffect();
        //if (effect instanceof BuffEffectSilence) { //If silence then remove.
        event.setCancelled(true);
        resetCooldown(player.getUniqueId());
        //}
    }

    //TODO
    public void onSpellCast() {

    }

    @Override
    public void tick() {
        for (final UUID uuid : registered) {
            final Optional<Player> optional = Sponge.getServer().getPlayer(uuid);
            optional.ifPresent((Player player) -> {
                final PotionEffectData peData = player.get(PotionEffectData.class).orElseThrow(
                        () -> new IllegalStateException("Unable to get potion effect data!"));
                //peData.addElement((PotionEffect) new BuffEffectManaRegen(1, 2)); //Mana regen 2 | Safe cast as per sponge mixins.
                player.offer(peData);
            });
        }
        super.tick();
    }
}
