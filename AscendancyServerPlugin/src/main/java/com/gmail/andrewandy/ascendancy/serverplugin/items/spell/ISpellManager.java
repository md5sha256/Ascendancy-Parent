package com.gmail.andrewandy.ascendancy.serverplugin.items.spell;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ISpellManager {

    void registerSpell(@NotNull Spell spell);

    void unregisterSpell(@NotNull Spell spell);

    boolean isRegistered(@NotNull Spell spell);

    @NotNull Collection<Spell> getRegisteredSpells();

}
