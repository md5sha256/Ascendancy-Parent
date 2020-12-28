package com.gmail.andrewandy.ascendancy.serverplugin.items.spell;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

public interface Spell {

    @NotNull ItemStack getAsItemStack();

    boolean isSpell(@NotNull ItemStack itemStack);

    boolean willBounce();

    double getEffectValue();

    int getManaCost();

    @NotNull SecondaryShape getSecondaryShape();

    @NotNull Shape getShape();

    @NotNull Effect getEffect();

    @NotNull String getName();

    void castAs(@NotNull Player player);

    enum Shape {
        AOE, CONE, MELEE, PROJECTILE, SELF, ZONE
    }


    enum SecondaryShape {
        NONE, ZONE
    }


    enum Effect {
        AD, AP, BLIND, BLINDING_GRACE, WARRIORS_POISE, CHRONO_ANCHOR, DISARM, DISPEL, FURY, GOP, HEAL, LIFE_TAP, MANA_DRAIN, PHASER, ROOT, SHIELD, SILENCE, SLOW, SPEED, TRANSPLACE
    }

}
