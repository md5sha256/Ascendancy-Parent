package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.solace;

import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.ChallengerModule;
import com.gmail.andrewandy.ascendancy.lib.game.data.IChallengerData;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;

public class Solace extends AbstractChallenger {

    @Inject
    Solace(final SolaceComponentFactory componentFactory) {
        super("Solace", challenger -> abilities(challenger, componentFactory),
                challenger -> runes(challenger, componentFactory), ChallengerModule.getLoreOf("Solace"));
    }

    private static Ability[] abilities(@NotNull final Challenger challenger,
                                       @NotNull final SolaceComponentFactory factory) {
        return new Ability[]{factory.createCallbackOfTheAfterlife(challenger),
                factory.createUndiminishedSoul(challenger)};
    }

    private static PlayerSpecificRune[] runes(@NotNull final Challenger challenger, @NotNull final SolaceComponentFactory componentFactory) {
        return new PlayerSpecificRune[0];
    }

    @Override
    @NotNull
    public IChallengerData toData() {
        return null;
    }

}
