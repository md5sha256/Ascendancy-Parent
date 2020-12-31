package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.hilda;

import com.gmail.andrewandy.ascendancy.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendancy.lib.game.data.game.ChallengerDataImpl;
import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.ChallengerModule;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.KnavisComponentFactory;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;

public class Hilda extends AbstractChallenger {

    @Inject
    Hilda(HildaComponentFactory factory) {
        super("Hilda", challenger -> abilities(challenger, factory),
                challenger -> runes(challenger, factory), ChallengerModule.getLoreOf("Knavis"));
    }

    private static Ability[] abilities(final Challenger challenger,
                                       final HildaComponentFactory componentFactory) {
        return new Ability[]{componentFactory.createMirrorOfResolutionFor(challenger),
                componentFactory.createIdentityOfPurityFor(challenger)};
    }

    private static PlayerSpecificRune[] runes(final Challenger challenger,
                                              final HildaComponentFactory componentFactory) {
        return new PlayerSpecificRune[0];
    }

    @Override
    public IChallengerData toData() {
        try {
            return new ChallengerDataImpl(getName(), new File("Path to file on server"), getLore());
        } catch (final IOException ex) {
            throw new IllegalStateException("Unable to create ChampionData", ex);
        }
    }

}
