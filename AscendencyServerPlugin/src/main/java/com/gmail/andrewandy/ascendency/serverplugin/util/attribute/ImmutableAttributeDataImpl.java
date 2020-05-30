package com.gmail.andrewandy.ascendency.serverplugin.util.attribute;

import com.gmail.andrewandy.ascendency.serverplugin.api.attributes.AscendencyAttribute;
import com.gmail.andrewandy.ascendency.serverplugin.api.attributes.AttributeData;
import com.gmail.andrewandy.ascendency.serverplugin.api.attributes.ImmutableAttributeData;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.ValueFactory;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;

import java.util.HashMap;
import java.util.Map;

public class ImmutableAttributeDataImpl
    extends AbstractImmutableData<ImmutableAttributeData, AttributeData>
    implements ImmutableAttributeData {

    private final ImmutableMap<AscendencyAttribute, ImmutableBoundedValue<Integer>> attributeMap;
    private final ValueFactory factory = Sponge.getRegistry().getValueFactory();

    public ImmutableAttributeDataImpl(
        @NotNull final Map<AscendencyAttribute, ? extends BoundedValue<Integer>> values) {
        final Map<AscendencyAttribute, ImmutableBoundedValue<Integer>> temp =
            new HashMap<>(values.size());
        for (final Map.Entry<AscendencyAttribute, ? extends BoundedValue<Integer>> entry : values
            .entrySet()) {
            temp.put(entry.getKey(),
                entry.getKey().createBlankValue(entry.getValue().get()).asImmutable());
        }
        this.attributeMap = ImmutableMap.copyOf(temp);
    }

    @Override public @NotNull ImmutableBoundedValue<Integer> attribute(
        @NotNull final AscendencyAttribute attribute) {
        return attributeMap.get(attribute);
    }

    @Override protected void registerGetters() {}

    @Override @NotNull public AttributeData asMutable() {
        return new AttributeDataImpl(attributeMap);
    }

    @Override public int getContentVersion() {
        return 0;
    }
}
