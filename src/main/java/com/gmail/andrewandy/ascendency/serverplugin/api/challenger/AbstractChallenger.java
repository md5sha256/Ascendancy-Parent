package com.gmail.andrewandy.ascendency.serverplugin.api.challenger;

import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractChallenger implements Challenger {

    @NotNull private final String name;
    @NotNull private final PlayerSpecificRune[] runes;
    @NotNull private final List<String> lore;
    @NotNull private final Ability[] abilities;

    public AbstractChallenger(@NotNull final String name, @NotNull final Ability[] abilities, @NotNull final PlayerSpecificRune[] runes,
        final List<String> lore) {
        this.name = name;
        this.runes = runes;
        this.abilities = abilities;
        this.lore = lore;
    }

    @Override @NotNull public Ability[] getAbilities() {
        return abilities;
    }

    @Override @NotNull public String getName() {
        return name;
    }

    @Override @NotNull public PlayerSpecificRune[] getRunes() {
        return runes;
    }

    @Override @NotNull public List<String> getLore() {
        return lore;
    }


}
