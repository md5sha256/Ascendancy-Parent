package com.gmail.andrewandy.ascendancy.serverplugin.items.spell;

import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.entity.MoveEntityEvent;

@FunctionalInterface
public interface SpellCondition {

    boolean isConditionMet(final Spell spell, final Projectile projectile);

    @FunctionalInterface
    interface Handler {

        void onSpellProjectileMove(final Spell spell, final MoveEntityEvent event);

    }

}
