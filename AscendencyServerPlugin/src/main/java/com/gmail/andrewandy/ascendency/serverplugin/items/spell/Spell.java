package com.gmail.andrewandy.ascendency.serverplugin.items.spell;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

public interface Spell {

    ItemStack getAsItemStack();

    boolean isSpell(ItemStack itemStack);

    boolean willBounce();

    double getEffectValue();

    int getManaCost();

    SecondaryShape getSecondaryShape();

    Shape getShape();

    Effect getEffect();

    String getName();

    void castAs(Player player);

    enum Shape {
        AOE,
        CONE,
        MELEE,
        PROJECTILE,
        SELF,
        ZONE
    }

    enum SecondaryShape {
        NONE,
        ZONE
    }

    enum Effect {
        AD,
        AP,
        BLIND,
        BLINDING_GRACE,
        WARRIORS_POISE,
        CHRONO_ANCHOR,
        DISARM,
        DISPEL,
        FURY,
        GOP,
        HEAL,
        LIFE_TAP,
        MANA_DRAIN,
        PHASER,
        ROOT,
        SHIELD,
        SILENCE,
        SLOW,
        SPEED,
        TRANSPLACE
    }

}
