package com.gmail.andrewandy.ascendency.serverplugin.api.mechanics;

import com.gmail.andrewandy.ascendency.lib.util.CommonUtils;
import com.gmail.andrewandy.ascendency.serverplugin.api.attributes.AscendencyAttribute;
import com.gmail.andrewandy.ascendency.serverplugin.api.attributes.AttributeData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.entity.damage.DamageType;

public enum AscendencyDamageTypes implements DamageType {

    ATTACK_DAMAGE {
        //AD final damage = AD x baseDamage x (1-(0.05 x |Armor-APen|))
        @Override public double calculateDamageFor(final @NotNull Player victim,
            final @NotNull Player attacker, final double baseDamage) {
            final AttributeData victimData = victim.getOrCreate(AttributeData.class)
                .orElseThrow(() -> unableToFindAttributeData);
            final AttributeData attackerData = victim.getOrCreate(AttributeData.class)
                .orElseThrow(() -> unableToFindAttributeData);
            int armor = victimData.getAttributePrimitive(AscendencyAttribute.ARMOR);
            int armorPen =
                attackerData.getAttributePrimitive(AscendencyAttribute.ARMOR_PENETRATION);
            int attackDamage =
                attackerData.getAttributePrimitive(AscendencyAttribute.ATTACK_DAMAGE);
            return baseDamage * attackDamage * (1 - (0.05 * Math.max(armor - armorPen, 0)));
        }
    }, MAGIC {
        //AP final damage = AP x baseDamage x (1-(0.05 x |MR-MPen|))
        @Override public double calculateDamageFor(@NotNull final Player victim,
            @NotNull final Player attacker, final double baseDamage) {

            final AttributeData victimData = victim.getOrCreate(AttributeData.class)
                .orElseThrow(() -> unableToFindAttributeData);
            final AttributeData attackerData = victim.getOrCreate(AttributeData.class)
                .orElseThrow(() -> unableToFindAttributeData);

            final int magicRes =
                victimData.getAttributePrimitive(AscendencyAttribute.MAGIC_RESISTANCE);
            final int magicPen =
                attackerData.getAttributePrimitive(AscendencyAttribute.MAGIC_PENETRATION);
            final int abilityPower =
                attackerData.getAttributePrimitive(AscendencyAttribute.ABILITY_POWER);

            return baseDamage * abilityPower * (1 - (0.05 * Math.max(magicRes - magicPen, 0)));
        }
    }, TRUE;

    //Exceptions are expensive thus, we make one and save it here.
    private static final RuntimeException unableToFindAttributeData =
        new IllegalStateException("Unable to get ascendency attributes!");

    @Override @NotNull public String getId() {
        return "ascendencyserverplugin:" + name().toLowerCase();
    }

    @Override @NotNull public String getName() {
        return CommonUtils.capitalise(name().toLowerCase().replace("_", " "));
    }

    /**
     * Calculate the damage in context based off of a player's attributes.
     *
     * @param victim     The person to damage.
     * @param attacker   The attacker (damage source)
     * @param baseDamage The base damage to deal.
     * @return Returns the modified damage to deal to the player.
     */
    public double calculateDamageFor(@NotNull final Player victim, @NotNull final Player attacker,
        double baseDamage) {
        return baseDamage;
    }
}
