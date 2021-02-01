package com.gmail.andrewandy.ascendancy.serverplugin.api.mechanics;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.entity.damage.DamageType;

public abstract class AscendancyDamageType implements DamageType {


    private static String generateId(@NotNull String name) {
        return "ascendancyserverplugin:" + name;
    }

    private final String name;
    private final String id;

    AscendancyDamageType(@NotNull String name ) {
        this.name = name;
        this.id = generateId(name);
    }

    @Override
    @NotNull
    public String getId() {
        return id;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Calculate the damage in context based off of a player's attributes.
     *
     * @param victim     The person to damage.
     * @param attacker   The attacker (damage source)
     * @param baseDamage The base damage to deal.
     * @return Returns the modified damage to deal to the player.
     */
    public abstract double calculateDamageFor(@NotNull final Player victim, @NotNull final Player attacker, double baseDamage);

}
