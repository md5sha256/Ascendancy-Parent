package com.gmail.andrewandy.ascendancy.serverplugin.api.mechanics;

import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.AscendancyAttribute;
import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.AttributeData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AscendancyDamageUtil {

    private final Map<DamageType, DamageCalculator> handlerMap = new HashMap<>();

    AscendancyDamageUtil() {
        registerDefaults();
    }

    private void registerDefaults() {
        registerDamageCalculator(DamageImpls.ATTACK_DAMAGE, DamageImpls.ATTACK_DAMAGE::calculateDamageFor);
        registerDamageCalculator(DamageImpls.MAGIC, DamageImpls.MAGIC::calculateDamageFor);
        registerDamageCalculator(DamageImpls.TRUE, DamageImpls.TRUE::calculateDamageFor);
    }

    void registerDamageCalculator(@NotNull DamageType damageType, @NotNull DamageCalculator calculator) {
        this.handlerMap.put(damageType, Objects.requireNonNull(calculator));
    }

    public double damage(@NotNull Player victim, @NotNull Player attacker, double baseDamage, @NotNull DamageType damageType) {
        final DamageCalculator calculator = handlerMap.get(damageType);
        if (calculator == null) {
            throw new IllegalArgumentException("No damage calculator registered for DamageType: " + damageType);
        }
        final DamageSource damageSource = DamageSource.builder().bypassesArmor().type(damageType).absolute().build();
        final AttributeData attributeData = victim.get(AttributeData.class)
                .orElseThrow(() -> new IllegalStateException("Failed to get AttributeData for Player: " + victim.getName()));
        final double finalDamage = calculator.calculateDamage(victim, attacker, baseDamage) * attributeData.getAttributePrimitive(
                AscendancyAttribute.DAMAGE_REDUCTION) / 100D;
        victim.damage(finalDamage, damageSource);
        return finalDamage;
    }

    public interface DamageCalculator {

        /**
         * Calculate the damage in context based off of a player's attributes.
         *
         * @param victim     The person to damage.
         * @param attacker   The attacker (damage source)
         * @param baseDamage The base damage to deal.
         * @return Returns the modified damage to deal to the player.
         */
        double calculateDamage(
                @NotNull Player victim, @NotNull Player attacker,
                double baseDamage
        );

    }


}
