package com.gmail.andrewandy.ascendency.serverplugin.items.spell;

import com.gmail.andrewandy.ascendency.lib.util.CommonUtils;
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

    Spells(Shape shape, SecondaryShape secondaryShape, Effect effect, double effectValue, int manaCost, boolean willBounce) {
        this.shape = shape;
        this.secondaryShape = secondaryShape;
        this.effect = effect;
        this.effectValue = effectValue;
        this.manaCost = manaCost;
        this.willBounce = willBounce;
    }

    Spells(Shape shape, SecondaryShape secondaryShape, Effect effect, double effectValue, int manaCost) {
        this(shape, secondaryShape, effect, effectValue, manaCost, false);
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
    public String getName() {
        return CommonUtils.capitalise(name().toLowerCase()).replace("_"," ");
    }

    @Override
    public Effect getEffect() {
        return this.effect;
    }

    @Override
    public Shape getShape() {
        return this.shape;
    }

    @Override
    public SecondaryShape getSecondaryShape() {
        return this.secondaryShape;
    }

    @Override
    public boolean isSpell(ItemStack itemStack) {
        Optional<?> name = itemStack.get(Keys.DISPLAY_NAME);
        return name.filter(o -> ((String) o).equalsIgnoreCase(getName())).isPresent();
    }

    @Override
    public void castAs(Player player) {
        Shape shape = this.getShape();
        SecondaryShape secondaryShape = this.getSecondaryShape();
        Effect effect = this.getEffect();
        double effectValue = this.getEffectValue();
        int manaCost = this.getManaCost();

        //define apply effect methods here


        //define shapes here
        if (shape == Shape.PROJECTILE) {

        }

    }

}
