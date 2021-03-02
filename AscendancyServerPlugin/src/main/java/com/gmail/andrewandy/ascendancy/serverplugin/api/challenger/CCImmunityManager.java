package com.gmail.andrewandy.ascendancy.serverplugin.api.challenger;

import com.gmail.andrewandy.ascendancy.serverplugin.util.game.Tickable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.entity.Entity;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Represents a crowd-control immunity manager.
 * Implementations of this class should listen to {@link org.spongepowered.api.event.entity.ChangeEntityPotionEffectEvent.Gain}
 * to enforce immunity.
 */
public interface CCImmunityManager extends Tickable {

    IllegalStateException potionDataNotFound =
            new IllegalStateException("Unable to obtain PotionEffectData!");

    /**
     * Get a collection of all effects this manager deems to be
     * of crowd-control.
     *
     * @return Returns a shallow-cloned copy of {@link PotionEffectType}s which this
     *         manager recognises as crowd-control effects.
     */
    @NotNull Collection<PotionEffectType> getRegisteredEffects();

    /**
     * Register a {@link PotionEffectType} as a crowd-control effect this manager should check for.
     *
     * @param effectType The instance of the {@link PotionEffectType} to register.
     */
    void registerCrowdControlEffect(@NotNull PotionEffectType effectType);

    /**
     * Unregister a {@link PotionEffectType} as a crowd-control effect this manager should check for.
     *
     * @param effectType The instance of the {@link PotionEffectType} to unregister.
     */
    void unregisterCrowdControlEffect(@NotNull PotionEffectType effectType);

    /**
     * Check whether a given potion effect is recognised as a crowd-control effect
     * by this manager.
     *
     * @param effectType The {@link PotionEffectType} to check for.
     * @return Returns whether the specified effect type is recognised as a crowd-control
     *         effect. Implementations of this method should ensure that if the result is <code>true</code>,
     *         {@link #getRegisteredEffects()} MUST contain this effect type.
     */
    boolean isCrowdControl(@NotNull PotionEffectType effectType);

    /**
     * Check whether an entity is immune to crowd-control effects.
     *
     * @param entity The instance of the entity.
     * @return Returns whether the entity is immune to crowd-control effects.
     */
    boolean isImmune(@NotNull Entity entity);

    /**
     * Get the remaining time an entity is immune to crowd-control effects.
     *
     * @param entity   The entity to check for.
     * @param timeUnit The time unit to convert to.
     * @return Returns the remaining time the entity is immune to crown control for. This number should
     *         ALWAYS be >= 0.
     */
    long getImmunityDuration(@NotNull Entity entity, @NotNull TimeUnit timeUnit);

    /**
     * Force an entity to be immune from crowd-control effects for a given duration.
     *
     * @param entity   The instance of the entity.
     * @param duration The duration to be immune for, must be > 0.
     * @param timeUnit The time unit of the duration.
     * @throws IllegalArgumentException Thrown if the duration is < 1.
     */
    void setImmune(@NotNull Entity entity, long duration, @NotNull TimeUnit timeUnit)
            throws IllegalArgumentException;

    /**
     * Utility method to apply an effect to an entity. This method will check {@link #isCrowdControl(PotionEffectType)}
     * and {@link #isImmune(Entity). If either return true, this method will return false. }
     *
     * @param effect The potion effect to apply.
     * @param entity The entity to apply to.
     * @return Returns whether the effect was applied based off of Crowd-Control immunity.
     * @throws IllegalStateException Thrown if {@link Entity#getOrCreate(Class)} is empty - meaning the potion data
     *                               could not be found.
     */
    default boolean applyEffect(@NotNull final PotionEffect effect, @NotNull final Entity entity)
            throws IllegalStateException {
        if (isImmune(entity) && isCrowdControl(effect.getType())) {
            return false;
        }
        final PotionEffectData data =
                entity.getOrCreate(PotionEffectData.class).orElseThrow(() -> potionDataNotFound);
        data.addElement(effect);
        entity.offer(data);
        return true;
    }

    /**
     * Reset an entity based off it's UUID. This method is useful for
     * entities who have de-spawned.
     *
     * @param uniqueID The UniqueID to remove.
     */
    void reset(@NotNull UUID uniqueID);

    /**
     * Clear all registered types of crowd-control {@link PotionEffectType}s
     */
    void clearRegisteredCC();

    /**
     * Clear all entities which are {@link #isImmune(Entity)}
     */
    void clearRegisteredImmuneEntities();

    /**
     * Reset this manager to the factory state.
     */
    default void clearAll() {
        clearRegisteredCC();
        clearRegisteredImmuneEntities();
    }

}
