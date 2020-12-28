package com.gmail.andrewandy.ascendancy.serverplugin.api.ability;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.ChallengerUtils;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Represents an {@link Ability} with a cooldown.
 */
public abstract class AbstractCooldownAbility extends AbstractTickableAbility {

    private final long cooldown;
    protected Map<UUID, Long> cooldownMap = new HashMap<>();
    private Predicate<Map.Entry<UUID, Long>> tickHandler;

    public AbstractCooldownAbility(final String name, final boolean isActive, final long cooldown,
                                   final TimeUnit timeUnit, final Challenger bound) {
        super(name, isActive, bound);
        this.cooldown = Common.toTicks(cooldown, timeUnit);
        this.tickHandler = ChallengerUtils.mapTickPredicate(this.cooldown, null);
    }

    public Predicate<Map.Entry<UUID, Long>> getTickHandler() {
        return tickHandler;
    }

    public void setTickHandler(final Predicate<Map.Entry<UUID, Long>> mapTickPredicate) {
        this.tickHandler = Objects.requireNonNull(mapTickPredicate);
    }

    public Map<UUID, Long> getCooldowns() {
        return cooldownMap;
    }

    /**
     * Whether or not a player is on cooldown.
     *
     * @param player The UniqueID of a player.
     * @return Returns whether the player is currenltly on cooldown.
     */
    public boolean isOnCooldown(final UUID player) {
        return getCooldownDuration(TimeUnit.MILLISECONDS) != 0L;
    }

    /**
     * Get the cooldown the player is currently on in ticks.
     *
     * @param player The UniqueID of the player.
     * @return Returns the cooldown the player is on, {{@link #getCooldownDuration()}} represents there is no cooldown remaining.
     */
    public long getCooldownFor(final UUID player) {
        if (!cooldownMap.containsKey(player)) {
            return getCooldownDuration();
        }
        return cooldownMap.get(player);
    }

    public void setCooldownFor(final UUID player, final long cooldown, final TimeUnit timeUnit) {
        setCooldownFor(player, Common.toTicks(cooldown, timeUnit));
    }

    public void setCooldownFor(final UUID player, final long cooldown) {
        cooldownMap.remove(player);
        if (cooldown != 0) {
            cooldownMap.put(player, cooldown);
        }
    }

    /**
     * Clears this player's cooldown. This will only take effect in the next tick!
     */
    public void clearCooldown(final UUID player) {
        cooldownMap.put(player, getCooldownDuration());
    }

    /**
     * Resets the cooldown ticker to 0, this means their cooldown has restarted.
     *
     * @param player
     */
    public void resetCooldown(final UUID player) {
        setCooldownFor(player, 0);
    }

    /**
     * Get the cooldown the player is currently on in a certain {@link TimeUnit}
     *
     * @param player   The UniqueID of the player.
     * @param timeUnit The {@link TimeUnit} to convert to
     * @return Returns the converter cooldown the player is currently on.
     */
    public long getCooldownFor(final UUID player, final TimeUnit timeUnit) {
        return Common.fromTicks(getCooldownFor(player), timeUnit);
    }

    /**
     * Get the converted cooldown duration this ability has.
     *
     * @param timeUnit The {@link TimeUnit} to convert to.
     * @return Returns the converted cooldown.
     */
    public long getCooldownDuration(final TimeUnit timeUnit) {
        return Common.fromTicks(cooldown, timeUnit);
    }

    public long getCooldownDuration() {
        return cooldown;
    }

    /**
     * The default ticker,
     */
    @Override
    public void tick() {
        cooldownMap.entrySet().removeIf(tickHandler);
    }
}
