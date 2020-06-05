package com.gmail.andrewandy.ascendency.serverplugin.api.effect;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;

import java.util.*;

public abstract class AbstractPotionEffect implements PotionEffect {

    private final PotionEffectType effectType;
    private final int duration, amplifier;
    private final boolean isAmbient, showParticles;
    private Map<Class<? extends Property<?, ?>>, Property<?, ?>> propertyMap = new HashMap<>();

    public AbstractPotionEffect(PotionEffectType effectType, int duration, int amplifier, boolean isAmbient, boolean showParticles) {
        this.effectType = effectType;
        this.duration = duration;
        this.amplifier = amplifier;
        this.isAmbient = isAmbient;
        this.showParticles = showParticles;
    }

    @Override public PotionEffectType getType() {
        return effectType;
    }

    @Override public int getDuration() {
        return duration;
    }

    @Override public int getAmplifier() {
        return amplifier;
    }

    @Override public boolean isAmbient() {
        return isAmbient;
    }

    @Override public boolean getShowParticles() {
        return showParticles;
    }

    @Override @NotNull
    public <T extends Property<?, ?>> Optional<T> getProperty(final Class<T> propertyClass) {
        if (!propertyMap.containsKey(propertyClass)) {
            return Optional.empty();
        }
        try {
            Object value = propertyMap.get(propertyClass);
            if (value != null) {
                assert propertyClass.isInstance(value);
                return Optional.of(propertyClass.cast(value));
            }
            return Optional.empty();
        } catch (ClassCastException ex) {
            return Optional.empty();
        }
    }

    @Override @NotNull public Collection<Property<?, ?>> getApplicableProperties() {
        return ImmutableSet.copyOf(propertyMap.values());
    }
}
