package com.gmail.andrewandy.ascendency.serverplugin.api.attributes;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;

public interface AttributeData extends DataManipulator<AttributeData, ImmutableAttributeData> {

    @NotNull MutableBoundedValue<Integer> attribute(@NotNull AscendencyAttribute attribute);

}
