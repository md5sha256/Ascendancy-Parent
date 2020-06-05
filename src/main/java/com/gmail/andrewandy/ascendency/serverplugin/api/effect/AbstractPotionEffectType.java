package com.gmail.andrewandy.ascendency.serverplugin.api.effect;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.api.text.translation.Translation;

public abstract class AbstractPotionEffectType implements PotionEffectType {

    private final boolean isInstant;
    private final Translation potionTranslation;
    private final String name, id;

    private static Translation generateFixedTranslation(String name) {
        return new FixedTranslation("ascendencyserverplugin:potion_effect_" + name);
    }

    public AbstractPotionEffectType(@NotNull final String name, @NotNull final String id,
        final boolean isInstant,
        @NotNull final Translation potionTranslation) {
        this.isInstant = isInstant;
        this.name = name;
        this.id = id;
        this.potionTranslation = potionTranslation;
    }

    public AbstractPotionEffectType(@NotNull final String name, @NotNull final String id, final boolean isInstant) {
        this(name, id, isInstant, generateFixedTranslation(name));
    }

    @Override public boolean isInstant() {
        return isInstant;
    }

    @Override @NotNull public Translation getPotionTranslation() {
        return potionTranslation;
    }

    @Override @NotNull public String getId() {
        return id;
    }

    @Override @NotNull public String getName() {
        return name;
    }

    @Override @NotNull public Translation getTranslation() {
        return potionTranslation;
    }
}
