package com.gmail.andrewandy.ascendency.serverplugin.api.attributes;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;

public interface ImmutableAttributeData
    extends ImmutableDataManipulator<ImmutableAttributeData, AttributeData> {

    @NotNull ImmutableBoundedValue<Integer> getAttribute(@NotNull AscendencyAttribute attribute);

    default int getAttributePrimitive(@NotNull AscendencyAttribute attribute) {
        final BoundedValue<Integer> boundedValue = getAttribute(attribute);
        return boundedValue.exists() ? attribute.defaultValue() : boundedValue.get();
    }
}
