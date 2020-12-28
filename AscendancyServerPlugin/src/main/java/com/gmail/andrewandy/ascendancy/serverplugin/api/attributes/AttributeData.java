package com.gmail.andrewandy.ascendancy.serverplugin.api.attributes;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;

public interface AttributeData extends DataManipulator<AttributeData, ImmutableAttributeData> {

    @NotNull MutableBoundedValue<Integer> getAttribute(@NotNull AscendancyAttribute attribute);

    default int getAttributePrimitive(@NotNull AscendancyAttribute attribute) {
        final MutableBoundedValue<Integer> boundedValue = getAttribute(attribute);
        return boundedValue.exists() ? attribute.defaultValue() : boundedValue.get();
    }

}
