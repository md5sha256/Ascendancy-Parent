package com.gmail.andrewandy.ascendency.serverplugin.api.attributes;

import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.value.ValueFactory;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Map;

public enum AscendencyAttribute {

    MAGIC_RESISTANCE(0, 0, 30000);

    private static final ValueFactory factory = Sponge.getRegistry().getValueFactory();

    public static Key<Value<AttributeData>> ASC_DATA_KEY = KeyFactory
        .makeSingleKey(TypeToken.of(AttributeData.class), new TypeToken<Value<AttributeData>>() {},
            DataQuery.of("AscendencyAttribute"), "ascenendencyserverplugin:asc_attr",
            "Ascendency Attributes");

    private final int defaultValue, max, min;
    private final Key<MutableBoundedValue<Integer>> key =KeyFactory.makeSingleKey(TypeToken.of(Integer.TYPE), new TypeToken<MutableBoundedValue<Integer>>() {
    }, DataQuery.of(name()), "ascendencyserverplugin:asc_atr_" + name().toLowerCase(), name());

    AscendencyAttribute(final int def, final int min, final int max) {
        this.defaultValue = def;
        this.max = max;
        this.min = min;
    }

    public Key<MutableBoundedValue<Integer>> getKey() {
        return key;
    }

    public int defualtValue() {
        return defaultValue;
    }

    public final MutableBoundedValue<Integer> createBlankValue() {
        return factory.createBoundedValueBuilder(key).defaultValue(defaultValue)
            .minimum(min).maximum(max).build();
    }
    public final MutableBoundedValue<Integer> createBlankValue(final int actualValue) {
        return factory.createBoundedValueBuilder(key).defaultValue(defaultValue)
            .actualValue(actualValue).minimum(min).maximum(max).build();
    }
}
