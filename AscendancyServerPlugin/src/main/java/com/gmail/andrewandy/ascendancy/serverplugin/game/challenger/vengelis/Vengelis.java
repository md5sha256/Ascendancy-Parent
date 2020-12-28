package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.vengelis;

import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.ChallengerModule;
import com.gmail.andrewandy.ascendancy.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendancy.lib.game.data.game.ChallengerDataImpl;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class Vengelis extends AbstractChallenger {

    @Inject
    Vengelis(final VengelisComponentFactory componentFactory) {
        super("Vengelis", challenger -> abilities(challenger, componentFactory),
                challenger -> runes(challenger, componentFactory),
                ChallengerModule.getLoreOf("Vengelis"));
    }

    private static Ability[] abilities(@NotNull final Challenger challenger,
                                       @NotNull final VengelisComponentFactory factory) {
        return new Ability[]{factory.createGyrationFor(challenger),
                factory.createHauntingFuryFor(challenger)};
    }

    private static PlayerSpecificRune[] runes(@NotNull final Challenger challenger,
                                              @NotNull final VengelisComponentFactory factory) {
        return new PlayerSpecificRune[0];
    }


    @Override
    @NotNull
    public IChallengerData toData() {
        try {
            return new ChallengerDataImpl(getName(), new File("Some path"), getLore());
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
