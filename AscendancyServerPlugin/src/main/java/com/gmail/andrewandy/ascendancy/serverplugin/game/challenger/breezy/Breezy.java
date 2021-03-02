package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.breezy;

import com.gmail.andrewandy.ascendancy.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendancy.lib.game.data.game.ChallengerDataImpl;
import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.ChallengerModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class Breezy extends AbstractChallenger {

    @AssistedInject
    Breezy(@Assisted BreezyComponentFactory componentFactory) {
        super("Breezy", challenger -> abilities(challenger, componentFactory),
                challenger -> runes(challenger, componentFactory),
                ChallengerModule.getLoreOf("Breezy")
        );
    }

    private static Ability[] abilities(
            @NotNull final Challenger challenger,
            @NotNull final BreezyComponentFactory componentFactory
    ) {
        return new Ability[]{componentFactory.createOopsFor(challenger),
                componentFactory.createRuneBoomFor(challenger)};
    }

    private static PlayerSpecificRune[] runes(
            @NotNull final Challenger challenger,
            @NotNull final BreezyComponentFactory componentFactory
    ) {
        return new PlayerSpecificRune[0];
    }

    @Override
    public IChallengerData toData() {
        try {
            return new ChallengerDataImpl(getName(), new File("Some Path"),
                    ChallengerModule.getLoreOf(getName())
            );
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
