package com.gmail.andrewandy.ascendancy.serverplugin.items.spell;

import com.gmail.andrewandy.ascendancy.lib.util.CommonUtils;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

public enum Spells implements Spell {

    ENIGMATIC_BOLT(Shape.PROJECTILE, SecondaryShape.NONE, Effect.AP, 0.5, 300) {
        @Override
        public ItemStack getAsItemStack() {
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
    public boolean isSpell(final ItemStack itemStack) {
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
    public SecondaryShape getSecondaryShape() {
        return this.secondaryShape;
    }

    @Override
    public Shape getShape() {
        return this.shape;
    }

    @Override
    public Effect getEffect() {
        return this.effect;
    }

    @Override
    public String getName() {
        return CommonUtils.capitalise(name().toLowerCase()).replace("_", " ");
    }

    @Override
    public void castAs(final Player player) {
        final Shape shape = this.getShape();
        final SecondaryShape secondaryShape = this.getSecondaryShape();
        final Effect effect = this.getEffect();
        final double effectValue = this.getEffectValue();
        final int manaCost = this.getManaCost();

        //define apply effect methods here


        //define shapes here
        if (shape == Shape.PROJECTILE) {

        }

    }

}
