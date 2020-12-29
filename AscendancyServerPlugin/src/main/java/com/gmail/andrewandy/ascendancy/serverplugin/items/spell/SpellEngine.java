package com.gmail.andrewandy.ascendancy.serverplugin.items.spell;

import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;

@Singleton
public class SpellEngine implements ISpellEngine {

    private final Set<Spell> registeredSpells = new HashSet<>();
    private final Map<SpellCondition, Set<Consumer<Spell>>> conditionHandlerMap = new HashMap<>();

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
    public @NotNull Set<@NotNull Consumer<@NotNull Spell>> getHandlers(@NotNull SpellCondition condition) {
        final Set<Consumer<Spell>> handlers = conditionHandlerMap.get(condition);
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
    public void registerSpellConditionHandler(@NotNull SpellCondition condition, @NotNull Consumer<@NotNull Spell> handler) {
        conditionHandlerMap.computeIfAbsent(condition, (unused) -> new HashSet<>()).add(handler);
    }

    @Override
    public void removeSpellConditionHandler(SpellCondition condition, Consumer<@NotNull Spell> consumer) {
        final Collection<Consumer<Spell>> handlers = conditionHandlerMap.get(condition);
        if (handlers != null) {
            handlers.remove(consumer);
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

}
