package com.gmail.andrewandy.ascendency.serverplugin.game.challenger.knavis;

import com.gmail.andrewandy.ascendency.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendency.lib.game.data.game.ChallengerDataImpl;
import com.gmail.andrewandy.ascendency.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendency.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.ChallengerModule;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;

/**
 * Represents the Knavis challenger. All abiliities and runes for Knavis can be found here.
 * //TODO test in game!
 */
public class Knavis extends AbstractChallenger implements Challenger {

    @Inject Knavis(KnavisComponentFactory factory) {
        super("Knavis", challenger -> abilities(challenger, factory),
              challenger -> runes(challenger, factory), ChallengerModule.getLoreOf("Knavis"));
    }

    private static Ability[] abilities(final Challenger challenger,
                                       final KnavisComponentFactory componentFactory) {
        return new Ability[] {componentFactory.createShadowsRetreatFor(challenger),
            componentFactory.createLivingGiftFor(challenger)};
    }

    private static PlayerSpecificRune[] runes(final Challenger challenger,
                                              final KnavisComponentFactory componentFactory) {
        return new PlayerSpecificRune[] {
            componentFactory.createBlessingOfTeleportationFor(challenger),
            componentFactory.createChosenOfTEarthFor(challenger),
            componentFactory.createHeartOfTheDryadFor(challenger),};
    }

    @Override public IChallengerData toData() {
        try {
            return new ChallengerDataImpl(getName(), new File("Path to file on server"), getLore());
        } catch (final IOException ex) {
            throw new IllegalStateException("Unable to create ChampionData", ex);
        }
    }
}
