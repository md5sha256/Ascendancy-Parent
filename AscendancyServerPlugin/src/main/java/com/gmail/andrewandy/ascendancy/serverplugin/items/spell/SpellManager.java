package com.gmail.andrewandy.ascendancy.serverplugin.items.spell;

import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

@Singleton
public class SpellManager implements ISpellManager {

    private final Collection<Spell> registeredSpells = new HashSet<>();

    SpellManager() {

    }

    @Override
    public void registerSpell(@NotNull final Spell spell) {
        registeredSpells.remove(spell);
        registeredSpells.add(spell);
    }

    @Override
    public void unregisterSpell(@NotNull final Spell spell) {
        registeredSpells.remove(spell);
    }

    public boolean isRegistered(@NotNull final Spell spell) {
        return registeredSpells.contains(spell);
    }

    @NotNull
    public Collection<Spell> getRegisteredSpells() {
        return new ArrayList<>(registeredSpells);
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
        for (final Spell spell : registeredSpells) {
            if (spell.isSpell(itemStack)) {
                spell.castAs(player);
                break;
            }
        }
    }

}
