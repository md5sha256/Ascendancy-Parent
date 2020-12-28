package com.gmail.andrewandy.ascendancy.serverplugin.util;

import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.data.manipulator.mutable.entity.FallDistanceData;
import org.spongepowered.api.data.manipulator.mutable.entity.KnockbackData;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Tristate;

import java.util.Optional;

public class Listeners {

    private static final IllegalStateException nullFallDamage =
            new IllegalStateException("Unable to get fall damage data!");


    /**
     * Handles nullifying fall damage.
     */
    @Listener(order = Order.EARLY)
    @IsCancelled(value = Tristate.UNDEFINED)
    public void onMove(
            final MoveEntityEvent event) {
        final Entity player = event.getTargetEntity();
        if (!(player instanceof Player)) {
            return;
        }
        final FallDistanceData data =
                player.get(FallDistanceData.class).orElseThrow(() -> nullFallDamage);
        data.fallDistance().set(0f);
        player.offer(data);
    }

    /**
     * Handles players "punching" each other.
     */
    @Listener(order = Order.EARLY)
    public void onEmptyHit(final DamageEntityEvent event) {
        final Optional<Player> source =
                event.getCause().get(DamageEntityEvent.SOURCE, Player.class);
        if (!source.isPresent()) {
            return;
        }
        final Player player = source.get();
        final Optional<ItemStack> inHand = player.getItemInHand(HandTypes.MAIN_HAND);
        if (!inHand.isPresent()) { //If player isn't holding anything in their hand.
            event.setCancelled(true);
        }
    }

    /**
     * Handles anti-dropping by players.
     */
    @Listener(order = Order.EARLY)
    public void onItemDrop(final DropItemEvent event) {
        if (event.getCause().root() instanceof Player) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles anti-knockback and effectively disabled the
     * vanilla cooldown for being damaged.
     */
    @Listener(order = Order.EARLY)
    public void onHit(final DamageEntityEvent event) {
        final Entity entity = event.getTargetEntity();
        if (!(entity instanceof Player))
            return;
        final EntityPlayer player = (EntityPlayer) entity;
        player.hurtTime = 0; //Reset the player's damage cooldown to 0.
        final KnockbackData knockbackData = entity.getOrCreate(KnockbackData.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get knockback data!"));
        knockbackData.knockbackStrength().set(0); //Sett the knocback strength to 0.
        entity.offer(knockbackData);
    }

}
