package com.gmail.andrewandy.ascendancy.serverplugin.api.attributes;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.value.ValueFactory;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;

@SuppressWarnings("UnstableApiUsage")
public enum AscendancyAttribute {

    ABILITY_POWER(10, 0),
    MAGIC_RESISTANCE(0, 0),
    MAGIC_PENETRATION(0, 0),
    MANA_MAXIMUM(3000, 0),
    CURRENT_MANA(0, 0),
    MANA_REGENERATION(100, 0),
    ATTACK_DAMAGE(0, 0),
    //100 is one attack per second
    ATTACK_SPEED(100, 0),
    ARMOR(0, 0),
    ARMOR_PENETRATION(0, 0);

    private static final ValueFactory factory = Sponge.getRegistry().getValueFactory();

    public static Key<Value<AttributeData>> ASC_DATA_KEY = KeyFactory
            .makeSingleKey(TypeToken.of(AttributeData.class), new TypeToken<Value<AttributeData>>() {
                    }, DataQuery.of("AscendancyAttribute"), "ascenendancyserverplugin:asc_attr",
                    "Ascendancy Attributes");

    private final int defaultValue, max, min;
    private final Key<MutableBoundedValue<Integer>> key = KeyFactory
            .makeSingleKey(TypeToken.of(Integer.TYPE), new TypeToken<MutableBoundedValue<Integer>>() {
            }, DataQuery.of(name()), "ascendancyserverplugin:asc_atr_" + name().toLowerCase(), name());

    AscendancyAttribute(final int def, final int min, final int max) {
        this.defaultValue = def;
        this.max = max;
        this.min = min;
    }

    AscendancyAttribute(final int def, final int min) {
        this(def, min, 30000);
    }

    public Key<MutableBoundedValue<Integer>> getKey() {
        return key;
    }

    public int defaultValue() {
        return defaultValue;
    }

    public MutableBoundedValue<Integer> createBlankValue() {
        return factory.createBoundedValueBuilder(key).defaultValue(defaultValue).minimum(min)
                .maximum(max).build();
    }

    public final MutableBoundedValue<Integer> createBlankValue(final int actualValue) {
        return factory.createBoundedValueBuilder(key).defaultValue(defaultValue)
                .actualValue(actualValue).minimum(min).maximum(max).build();
    }
}
