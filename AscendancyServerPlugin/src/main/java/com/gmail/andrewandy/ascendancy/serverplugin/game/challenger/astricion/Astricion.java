package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.astricion;

import com.gmail.andrewandy.ascendancy.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendancy.lib.game.data.game.ChallengerDataImpl;
import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.ChallengerModule;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.astricion.components.RuneDiabolicResistance;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.astricion.components.RuneEmpoweringRage;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.astricion.components.RuneReleasedLimit;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.PlayerMatchManager;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class Astricion extends AbstractChallenger {

    @Inject
    Astricion(@NotNull final PlayerMatchManager matchManager, final AstricionComponentFactory componentFactory) {
        super("Astricion", (challenger -> abilities(challenger, componentFactory)), Astricion::runes,
                ChallengerModule.getLoreOf("Astricion")
        );
    }

    private static Ability[] abilities(
            final Challenger challenger,
            final AstricionComponentFactory componentFactory
    ) {
        return new Ability[]{componentFactory.createDemonicCapacityFor(challenger), componentFactory.createSuppressionFor(
                challenger)};
    }

    @Deprecated
    private static PlayerSpecificRune[] runes(final Challenger challenger) {
        return new PlayerSpecificRune[]{new RuneDiabolicResistance(challenger),
                new RuneEmpoweringRage(challenger), new RuneReleasedLimit(challenger)};

    }

    @Override
    @NotNull
    public IChallengerData toData() {
        try {
            return new ChallengerDataImpl(getName(), new File("Path to data"), getLore());
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
