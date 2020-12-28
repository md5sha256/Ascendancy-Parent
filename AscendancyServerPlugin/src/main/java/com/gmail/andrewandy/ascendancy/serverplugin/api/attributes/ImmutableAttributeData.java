package com.gmail.andrewandy.ascendancy.serverplugin.api.attributes;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;

public interface ImmutableAttributeData
        extends ImmutableDataManipulator<ImmutableAttributeData, AttributeData> {

    @NotNull ImmutableBoundedValue<Integer> getAttribute(@NotNull AscendancyAttribute attribute);

    default int getAttributePrimitive(@NotNull AscendancyAttribute attribute) {
        final BoundedValue<Integer> boundedValue = getAttribute(attribute);
        return boundedValue.exists() ? attribute.defaultValue() : boundedValue.get();
    }
}
