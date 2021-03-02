package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.astricion.components;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.AbstractRune;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.living.player.Player;

public class RuneDiabolicResistance extends AbstractRune {

    public RuneDiabolicResistance(@NotNull final Challenger bound) {
        super(bound);
    }

    @Override
    public void applyTo(final Player player) {

    }

    @Override
    public void clearFrom(final Player player) {
    }

    @Override
    public @NotNull String getName() {
        return "DiabolicalResistance";
    }

    @Override
    public void tick() {

    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    @NotNull
    public DataContainer toContainer() {
        return null;
    }

}
