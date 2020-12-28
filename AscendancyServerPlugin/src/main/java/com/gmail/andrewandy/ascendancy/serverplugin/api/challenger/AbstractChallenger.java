package com.gmail.andrewandy.ascendancy.serverplugin.api.challenger;

import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.PlayerSpecificRune;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractChallenger implements Challenger {

    @NotNull
    private final String name;
    @NotNull
    private final PlayerSpecificRune[] runes;
    @NotNull
    private final List<String> lore;
    @NotNull
    private final Ability[] abilities;

    public AbstractChallenger(@NotNull final String name, @NotNull final Ability[] abilities,
                              @NotNull final PlayerSpecificRune[] runes,
                              @NotNull final List<String> lore) {
        this.name = name;
        this.runes = runes;
        this.abilities = abilities;
        this.lore = lore;
    }

    public AbstractChallenger(@NotNull final String name,
                              @NotNull final Function<Challenger, Ability[]> abilities,
                              @NotNull final Function<Challenger, PlayerSpecificRune[]> runes,
                              @NotNull final List<String> lore) {
        this.name = name;
        this.runes = Objects.requireNonNull(runes).apply(this);
        this.abilities = Objects.requireNonNull(abilities).apply(this);
        this.lore = lore;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    @NotNull
    public PlayerSpecificRune[] getRunes() {
        return runes;
    }

    @Override
    @NotNull
    public List<String> getLore() {
        return lore;
    }

    @Override
    @NotNull
    public Ability[] getAbilities() {
        return abilities;
    }


}
