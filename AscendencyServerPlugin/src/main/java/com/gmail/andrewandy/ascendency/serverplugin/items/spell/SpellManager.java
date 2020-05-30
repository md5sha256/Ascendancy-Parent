package com.gmail.andrewandy.ascendency.serverplugin.items.spell;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public enum SpellManager {

    INSTANCE;

    private Collection<Spell> registeredSpells = new HashSet<>();

    public void registerSpell(Spell spell) {
        registeredSpells.remove(spell);
        registeredSpells.add(spell);
    }

    public void unregisterSpell(Spell spell) {
        registeredSpells.remove(spell);
    }

    @Listener
    public void onClick(HandInteractEvent event) {
        final Object root = event.getCause().root();
        if (!(root instanceof Player)) {
            return;
        }
        final Player player = (Player) root;
        Optional<ItemStack> clicked = player.getItemInHand(event.getHandType());
        if (!clicked.isPresent()) {
            return;
        }
        ItemStack itemStack = clicked.get();
        for (Spell spell: registeredSpells) {
            if (spell.isSpell(itemStack)) {
                spell.castAs(player);
                break;
            }
        }
    }

}
