package com.gmail.andrewandy.ascendancy.serverplugin.util.game;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.CCImmunityManager;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.ChangeEntityPotionEffectEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Singleton
public class AscendancyCCManager implements CCImmunityManager {

    private final UUID uuid = UUID.randomUUID();
    private final Collection<PotionEffectType> registeredCC = new HashSet<>();
    private final Map<UUID, Long> immunityMap = new HashMap<>();

    AscendancyCCManager() {

    }

    @Override
    public UUID getUniqueID() {
        return uuid;
    }

    @Override
    public void tick() {
        Iterator<Map.Entry<UUID, Long>> iterator = immunityMap.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<UUID, Long> entry = iterator.next();
            final long modifiedValue = entry.getValue() - 1; //Take one tick off.
            if (modifiedValue < 1) { //If 0 ticks left, remove.
                iterator.remove();
            }
            entry.setValue(modifiedValue); //Mutate the entry.
        }
    }


    @Override
    @NotNull
    public Collection<PotionEffectType> getRegisteredEffects() {
        return new ArrayList<>(registeredCC);
    }

    @Override
    public void registerCrowdControlEffect(@NotNull final PotionEffectType effectType) {
        registeredCC.remove(effectType);
        registeredCC.add(effectType);
    }

    @Override
    public void unregisterCrowdControlEffect(@NotNull final PotionEffectType effectType) {
        registeredCC.remove(effectType);
    }

    @Override
    public boolean isCrowdControl(@NotNull final PotionEffectType effectType) {
        return registeredCC.contains(effectType);
    }

    @Override
    public boolean isImmune(@NotNull final Entity entity) {
        if (immunityMap.containsKey(entity.getUniqueId())) {
            return immunityMap.get(entity.getUniqueId()) != 0;
        }
        return false;
    }

    @Override
    public long getImmunityDuration(@NotNull final Entity entity,
                                    @NotNull final TimeUnit timeUnit) {
        return Common.fromTicks(immunityMap.getOrDefault(entity.getUniqueId(), 0L), timeUnit);
    }

    @Override
    public void setImmune(@NotNull final Entity entity, final long duration,
                          @NotNull final TimeUnit timeUnit) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0!");
        }
        immunityMap.remove(entity.getUniqueId());
        immunityMap.put(entity.getUniqueId(), Common.toTicks(duration, timeUnit));
    }

    @Override
    public void reset(@NotNull final UUID uniqueID) {
        immunityMap.remove(uniqueID);
    }

    @Override
    public void clearRegisteredCC() {
        registeredCC.clear();
    }

    @Override
    public void clearRegisteredImmuneEntities() {
        immunityMap.clear();
    }

    @Listener(order = Order.EARLY)
    public void onPotionAdd(final ChangeEntityPotionEffectEvent.Gain event) {
        if (isImmune(event.getTargetEntity()) && isCrowdControl(
                event.getPotionEffect().getType())) {
            event.setCancelled(true);
        }
    }
}
