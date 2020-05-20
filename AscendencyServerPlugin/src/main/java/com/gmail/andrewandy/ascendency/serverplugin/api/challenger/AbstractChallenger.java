package com.gmail.andrewandy.ascendency.serverplugin.api.challenger;

import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;

import java.util.List;

public abstract class AbstractChallenger implements Challenger {

    private final String name;
    private final PlayerSpecificRune[] runes;
    private final List<String> lore;
    private Ability[] abilities;

    public AbstractChallenger(String name, Ability[] abilities, PlayerSpecificRune[] runes, List<String> lore) {
        this.name = name;
        this.runes = runes;
        this.lore = lore;
    }

    @Override
    public Ability[] getAbilities() {
        return abilities;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PlayerSpecificRune[] getRunes() {
        return runes;
    }

    @Override
    public List<String> getLore() {
        return lore;
    }


}
