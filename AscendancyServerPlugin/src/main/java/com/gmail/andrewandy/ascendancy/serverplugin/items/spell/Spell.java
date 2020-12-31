package com.gmail.andrewandy.ascendancy.serverplugin.items.spell;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

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

    /**
     * Cast a spell as a given player, applying all effects and spawning a projectile
     * as necessary.
     *
     * <strong>WARNING: This method should not be called by the user. Always call
     * <code>ISpellManager#castSpell(Spell, Player)</code>, otherwise spell physics
     * will break.
     * </strong>
     *
     * @param player The player to cast as.
     * @return Returns the projectile if this spell creates one.
     * @see ISpellEngine#castSpell(Spell, Player)
     */
    @NotNull Optional<@NotNull Projectile> castAs(@NotNull Player player);

    enum Shape {
        AOE, CONE, MELEE, PROJECTILE, SELF, ZONE
    }


    enum SecondaryShape {
        NONE, ZONE
    }


    enum Effect {
        AD,
        AP,
        NONE,
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
