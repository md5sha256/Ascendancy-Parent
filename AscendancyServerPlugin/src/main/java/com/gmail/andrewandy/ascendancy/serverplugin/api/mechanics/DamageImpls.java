package com.gmail.andrewandy.ascendancy.serverplugin.api.mechanics;

import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.AscendancyAttribute;
import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.AttributeData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

public class DamageImpls {

    public static final AscendancyDamageType ATTACK_DAMAGE = new DamageImpls.AttackDamage();
    public static final AscendancyDamageType MAGIC = new DamageImpls.MagicDamage();
    public static final AscendancyDamageType TRUE = new DamageImpls.TrueDamage();

    //Exceptions are expensive thus, we make one and save it here.
    private static final RuntimeException unableToFindAttributeData =
            new IllegalStateException("Unable to get ascendency attributes!");

    public static final class AttackDamage extends AscendancyDamageType {

        private AttackDamage() {
            super("Attack Damage");
        }

        @Override
        public double calculateDamageFor(@NotNull Player victim, @NotNull Player attacker, double baseDamage) {
            final AttributeData victimData = victim.getOrCreate(AttributeData.class)
                    .orElseThrow(() -> unableToFindAttributeData);
            final AttributeData attackerData = victim.getOrCreate(AttributeData.class)
                    .orElseThrow(() -> unableToFindAttributeData);
            int armor = victimData.getAttributePrimitive(AscendancyAttribute.ARMOR);
            int armorPen =
                    attackerData.getAttributePrimitive(AscendancyAttribute.ARMOR_PENETRATION);
            int attackDamage =
                    attackerData.getAttributePrimitive(AscendancyAttribute.ATTACK_DAMAGE);
            return baseDamage * attackDamage * (1 - (0.05 * Math.max(armor - armorPen, 0)));
        }

    }

    public static final class MagicDamage extends AscendancyDamageType {

        private MagicDamage() {
            super("Magic Damage");
        }

        @Override
        public double calculateDamageFor(@NotNull Player victim, @NotNull Player attacker, double baseDamage) {
            final AttributeData victimData = victim.getOrCreate(AttributeData.class)
                    .orElseThrow(() -> unableToFindAttributeData);
            final AttributeData attackerData = victim.getOrCreate(AttributeData.class)
                    .orElseThrow(() -> unableToFindAttributeData);

            final int magicRes =
                    victimData.getAttributePrimitive(AscendancyAttribute.MAGIC_RESISTANCE);
            final int magicPen =
                    attackerData.getAttributePrimitive(AscendancyAttribute.MAGIC_PENETRATION);
            final int abilityPower =
                    attackerData.getAttributePrimitive(AscendancyAttribute.ABILITY_POWER);

            return baseDamage * abilityPower * (1 - (0.05 * Math.max(magicRes - magicPen, 0)));
        }

    }

    public static final class TrueDamage extends AscendancyDamageType {

        private TrueDamage() {
            super("True Damage");
        }

        @Override
        public double calculateDamageFor(@NotNull Player victim, @NotNull Player attacker, double baseDamage) {
            return baseDamage;
        }

    }

}
