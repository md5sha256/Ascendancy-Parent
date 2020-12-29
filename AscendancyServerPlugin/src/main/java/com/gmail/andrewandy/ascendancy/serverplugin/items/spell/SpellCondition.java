package com.gmail.andrewandy.ascendancy.serverplugin.items.spell;

import org.spongepowered.api.entity.projectile.Projectile;

@FunctionalInterface
public interface SpellCondition {

    boolean isConditionMet(final Spell spell);

    @FunctionalInterface
    interface Handler {

        void onSpellProjectileMove(final Spell spell, final Projectile projectile);

    }

}
