package com.gmail.andrewandy.ascendancy.serverplugin.items.spell;

import com.gmail.andrewandy.ascendancy.lib.util.CommonUtils;
import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.AscendancyAttribute;
import com.gmail.andrewandy.ascendancy.serverplugin.api.attributes.AttributeData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

public enum Spells implements Spell {

    ENIGMATIC_BOLT(Shape.PROJECTILE, SecondaryShape.NONE, Effect.AP, 0.5, 300) {
        @Override
        public @NotNull ItemStack getAsItemStack() {
            return null;
        }
    };

    private final boolean willBounce;
    private final double effectValue;
    private final int manaCost;
    private final Shape shape;
    private final SecondaryShape secondaryShape;
    private final Effect effect;

    Spells(final Shape shape, final SecondaryShape secondaryShape, final Effect effect,
           final double effectValue, final int manaCost, final boolean willBounce) {
        this.shape = shape;
        this.secondaryShape = secondaryShape;
        this.effect = effect;
        this.effectValue = effectValue;
        this.manaCost = manaCost;
        this.willBounce = willBounce;
    }

    Spells(final Shape shape, final SecondaryShape secondaryShape, final Effect effect,
           final double effectValue, final int manaCost) {
        this(shape, secondaryShape, effect, effectValue, manaCost, false);
    }

    @Override
    public boolean isSpell(final @NotNull ItemStack itemStack) {
        final Optional<?> name = itemStack.get(Keys.DISPLAY_NAME);
        return name.filter(o -> ((String) o).equalsIgnoreCase(getName())).isPresent();
    }

    @Override
    public boolean willBounce() {
        return this.willBounce;
    }

    @Override
    public double getEffectValue() {
        return this.effectValue;
    }

    @Override
    public int getManaCost() {
        return this.manaCost;
    }

    @Override
    public @NotNull SecondaryShape getSecondaryShape() {
        return this.secondaryShape;
    }

    @Override
    public @NotNull Shape getShape() {
        return this.shape;
    }

    @Override
    public @NotNull Effect getEffect() {
        return this.effect;
    }

    @Override
    public @NotNull String getName() {
        return CommonUtils.capitalise(name().toLowerCase()).replace("_", " ");
    }

    @Override
    public @NotNull Optional<@NotNull Projectile> castAs(final Player player) {
        final Shape shape = getShape();
        final SecondaryShape secondaryShape = getSecondaryShape();
        final Effect effect = getEffect();
        final double effectValue = getEffectValue();
        final int manaCost = getManaCost();

        final AttributeData attributeData = player.get(AttributeData.class)
                .orElseThrow(() -> new IllegalStateException("Failed to get attribute data for player: " + player.getName()));
        final MutableBoundedValue<Integer> mana = attributeData.getAttribute(AscendancyAttribute.CURRENT_MANA);
        mana.set(Math.min(mana.get() - manaCost, mana.getMinValue()));
        player.offer(attributeData);

        //define apply effect methods here

        //define shapes here
        switch (shape) {

        }
        return Optional.empty();
    }

}
