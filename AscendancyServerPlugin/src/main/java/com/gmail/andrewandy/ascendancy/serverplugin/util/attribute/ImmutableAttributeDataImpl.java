package com.gmail.andrewandy.ascendancy.serverplugin.util.attribute;

import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.AscendancyAttribute;
import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.AttributeData;
import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.ImmutableAttributeData;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.ValueFactory;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;

import java.util.HashMap;
import java.util.Map;

public class ImmutableAttributeDataImpl
        extends AbstractImmutableData<ImmutableAttributeData, AttributeData>
        implements ImmutableAttributeData {

    private final ImmutableMap<AscendancyAttribute, ImmutableBoundedValue<Integer>> attributeMap;
    private final ValueFactory factory = Sponge.getRegistry().getValueFactory();

    public ImmutableAttributeDataImpl(
            @NotNull final Map<AscendancyAttribute, ? extends BoundedValue<Integer>> values
    ) {
        final Map<AscendancyAttribute, ImmutableBoundedValue<Integer>> temp =
                new HashMap<>(values.size());
        for (final Map.Entry<AscendancyAttribute, ? extends BoundedValue<Integer>> entry : values
                .entrySet()) {
            temp.put(
                    entry.getKey(),
                    entry.getKey().createBlankValue(entry.getValue().get()).asImmutable()
            );
        }
        this.attributeMap = ImmutableMap.copyOf(temp);
    }

    @Override
    public @NotNull ImmutableBoundedValue<Integer> getAttribute(
            @NotNull final AscendancyAttribute attribute
    ) {
        return attributeMap.get(attribute);
    }

    @Override
    protected void registerGetters() {
    }

    @Override
    @NotNull
    public AttributeData asMutable() {
        return new AttributeDataImpl(attributeMap);
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

}
