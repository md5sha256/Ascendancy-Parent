package com.gmail.andrewandy.ascendency.serverplugin.api.challenger;

import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;

import java.util.List;

public abstract class AbstractChallenger implements Challenger {

    private final String name;
    private final PlayerSpecificRune[] runes;
    private final List<String> lore;
    private final Ability[] abilities;

    public AbstractChallenger(final String name, final Ability[] abilities, final PlayerSpecificRune[] runes,
        final List<String> lore) {
        this.name = name;
        this.runes = runes;
        this.abilities = abilities;
        this.lore = lore;
    }

    @Override public Ability[] getAbilities() {
        return abilities;
    }

    @Override public String getName() {
        return name;
    }

    @Override public PlayerSpecificRune[] getRunes() {
        return runes;
    }

    @Override public List<String> getLore() {
        return lore;
    }


}
