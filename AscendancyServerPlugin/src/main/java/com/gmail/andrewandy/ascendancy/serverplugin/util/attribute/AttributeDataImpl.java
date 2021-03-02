package com.gmail.andrewandy.ascendancy.serverplugin.util.attribute;

import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.AscendancyAttribute;
import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.AttributeData;
import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.ImmutableAttributeData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AttributeDataImpl extends AbstractData<AttributeData, ImmutableAttributeData>
        implements AttributeData {

    private final Map<AscendancyAttribute, MutableBoundedValue<Integer>> attributeMap;

    public AttributeDataImpl() {

        this.attributeMap = new HashMap<>(AscendancyAttribute.values().length);
        //Fill map will default values.
        for (final AscendancyAttribute attribute : AscendancyAttribute.values()) {
            attributeMap.put(attribute, attribute.createBlankValue());
        }
    }

    public AttributeDataImpl(
            @NotNull final Map<AscendancyAttribute, ? extends BoundedValue<Integer>> valueMap
    ) {
        final Map<AscendancyAttribute, MutableBoundedValue<Integer>> temp =
                new HashMap<>(valueMap.size());
        //Populate map
        for (final Map.Entry<AscendancyAttribute, ? extends BoundedValue<Integer>> entry : valueMap
                .entrySet()) {
            temp.put(entry.getKey(), entry.getKey().createBlankValue(entry.getValue().get()));
        }
        this.attributeMap = temp;

    }

    @Override
    protected void registerGettersAndSetters() {
    }

    @Override
    @NotNull
    public Optional<AttributeData> fill(
            @NotNull final DataHolder dataHolder,
            @NotNull final MergeFunction overlap
    ) {
        overlap.merge(this, dataHolder.get(AttributeData.class).orElse(null));
        return Optional.of(this);
    }

    @Override
    @NotNull
    public Optional<AttributeData> from(@NotNull final DataContainer container) {
        boolean hasAllKeys = true;
        for (final AscendancyAttribute attribute : AscendancyAttribute.values()) {
            if (container.contains(attribute.getKey())) {
                hasAllKeys = false;
                break;
            }
        }
        if (!hasAllKeys) {
            return Optional.empty();
        }
        this.attributeMap.clear();
        //Populate map
        for (final AscendancyAttribute attribute : AscendancyAttribute.values()) {
            final Optional<Integer> optional = container.getInt(attribute.getKey().getQuery());
            final MutableBoundedValue<Integer> value =
                    optional.map(attribute::createBlankValue).orElse(attribute.createBlankValue());
            this.attributeMap.put(attribute, value);
        }
        return Optional.of(this);
    }

    @Override
    @NotNull
    public AttributeData copy() {
        return new AttributeDataImpl(this.attributeMap);
    }

    @Override
    @NotNull
    public ImmutableAttributeData asImmutable() {
        return new ImmutableAttributeDataImpl(attributeMap);
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    @NotNull
    public MutableBoundedValue<Integer> getAttribute(@NotNull final AscendancyAttribute attribute) {
        return attributeMap.get(attribute);
    }

}
