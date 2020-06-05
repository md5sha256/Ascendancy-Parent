package com.gmail.andrewandy.ascendency.serverplugin.util;

import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.manipulator.mutable.entity.KnockbackData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DamageEntityEvent;

public class HitReset {

    @Listener(order = Order.EARLY)
    public void onHit(final DamageEntityEvent event) {
        final Entity entity = event.getTargetEntity();
        if (!(entity instanceof Player)) return;
        EntityPlayer player = (EntityPlayer) entity;
        player.hurtTime = 0;
        KnockbackData knockbackData = entity.getOrCreate(KnockbackData.class).orElseThrow(() -> new IllegalStateException("Unable to get knockback data!"));
        knockbackData.knockbackStrength().set(0);
        entity.offer(knockbackData);
    }

}
