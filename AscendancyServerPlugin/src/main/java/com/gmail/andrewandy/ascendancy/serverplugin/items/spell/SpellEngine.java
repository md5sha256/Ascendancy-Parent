package com.gmail.andrewandy.ascendancy.serverplugin.items.spell;

import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

@Singleton
public class SpellEngine implements ISpellEngine {

    private final Set<Spell> registeredSpells = new HashSet<>();
    private final Map<SpellCondition, Set<SpellCondition.Handler>> conditionHandlerMap = new HashMap<>();

    // Use a WeakHashMap to ensure that 'dead' projectiles get properly GC'ed.
    private final Map<Projectile, Spell> castedSpellMap = new WeakHashMap<>();

    SpellEngine() {

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
    public Set<Spell> getRegisteredSpells() {
        return new HashSet<>(registeredSpells);
    }

    @Override
    public @NotNull Set<@NotNull SpellCondition> getSpellConditions() {
        return new HashSet<>(conditionHandlerMap.keySet());
    }

    @Override
    public @NotNull Set<SpellCondition.@NotNull Handler> getHandlers(@NotNull SpellCondition condition) {
        final Set<SpellCondition.Handler> handlers = conditionHandlerMap.get(condition);
        return handlers == null ? Collections.emptySet() : new HashSet<>(handlers);
    }

    @Override
    public void registerSpellCondition(@NotNull SpellCondition condition) {
        conditionHandlerMap.putIfAbsent(condition, new HashSet<>());
    }

    @Override
    public void removeSpellCondition(@NotNull SpellCondition condition) {
        conditionHandlerMap.remove(condition);
    }

    @Override
    public void registerSpellConditionHandler(@NotNull SpellCondition condition, @NotNull SpellCondition.Handler handler) {
        conditionHandlerMap.computeIfAbsent(condition, (unused) -> new HashSet<>()).add(handler);
    }

    @Override
    public void removeSpellConditionHandler(@NotNull SpellCondition condition, @NotNull SpellCondition.Handler handler) {
        final Collection<SpellCondition.Handler> handlers = conditionHandlerMap.get(condition);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }

    @Override
    public void clearSpellConditionHandlers(@NotNull SpellCondition condition) {
        conditionHandlerMap.remove(condition);
    }

    @Override
    public void clearSpellConditions() {
        conditionHandlerMap.clear();
    }

    @Override
    public @NotNull Optional<Projectile> castSpell(@NotNull Spell spell, @NotNull Player caster) {
        final Optional<Projectile> optionalProjectile = spell.castAs(caster);
        optionalProjectile.ifPresent(projectile -> castedSpellMap.put(projectile, spell));
        return optionalProjectile;
    }

    @Override
    public @NotNull Optional<@NotNull Spell> getSpellFromProjectile(Projectile projectile) {
        return Optional.ofNullable(castedSpellMap.get(projectile));
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
                castSpell(spell, player);
                break;
            }
        }
    }

    @Listener(order = Order.EARLY)
    public void onProjectileMove(final MoveEntityEvent event) {
        final Entity entity = event.getTargetEntity();
        if (!(entity instanceof Projectile)) {
            return;
        }
        final Projectile projectile = (Projectile) entity;
        final Spell spell = castedSpellMap.get(projectile);
        if (spell == null) {
            return;
        }
        for (Map.Entry<SpellCondition, Set<SpellCondition.Handler>> entry : conditionHandlerMap.entrySet()) {
            if (entry.getKey().isConditionMet(spell, projectile)) {
                entry.getValue().forEach(handler -> handler.onSpellProjectileMove(spell, event));
            }
        }
    }

}
