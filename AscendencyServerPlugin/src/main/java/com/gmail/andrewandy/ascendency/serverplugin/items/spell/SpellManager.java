package com.gmail.andrewandy.ascendency.serverplugin.items.spell;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public enum SpellManager implements ISpellManager {

    INSTANCE;

    private final Collection<Spell> registeredSpells = new HashSet<>();

    public void registerSpell(final Spell spell) {
        registeredSpells.remove(spell);
        registeredSpells.add(spell);
    }

    public void unregisterSpell(final Spell spell) {
        registeredSpells.remove(spell);
    }

    @Listener
    public void onClick(final HandInteractEvent event) {
        final Object root = event.getCause().root();
        if (!(root instanceof Player)) {
            return;
        }
        final Player player = (Player) root;
        final Optional<ItemStack> clicked = player.getItemInHand(event.getHandType());
        if (!clicked.isPresent()) {
            return;
        }
        final ItemStack itemStack = clicked.get();
        for (final Spell spell: registeredSpells) {
            if (spell.isSpell(itemStack)) {
                spell.castAs(player);
                break;
            }
        }
    }

}
