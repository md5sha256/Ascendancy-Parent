package com.gmail.andrewandy.ascendency.serverplugin.util.attribute;

import com.gmail.andrewandy.ascendency.serverplugin.api.attributes.AscendencyAttribute;
import com.gmail.andrewandy.ascendency.serverplugin.api.attributes.AttributeData;
import com.gmail.andrewandy.ascendency.serverplugin.api.attributes.ImmutableAttributeData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

public class AttributeDataBuilder extends AbstractDataBuilder<AttributeData>
    implements DataManipulatorBuilder<AttributeData, ImmutableAttributeData> {

    public static final int CONTENT_VERSION = 0;

    public AttributeDataBuilder() {
        super(AttributeData.class, CONTENT_VERSION);
    }

    @Override @NotNull public AttributeData create() {
        return new AttributeDataImpl();
    }

    @Override @NotNull public Optional<AttributeData> createFrom(final DataHolder dataHolder) {
        return create().fill(dataHolder);
    }

    @Override @NotNull protected Optional<AttributeData> buildContent(final DataView container)
        throws InvalidDataException {

        final AttributeData data = new AttributeDataImpl();
        for (final AscendencyAttribute attribute : AscendencyAttribute.values()) {
            final Integer value =
                container.getInt(attribute.getKey().getQuery()).orElseThrow(() -> new InvalidDataException("Missing: " + attribute.getKey().getName()));
            data.attribute(attribute).set(value);
        }
        return Optional.of(data);
    }
}
