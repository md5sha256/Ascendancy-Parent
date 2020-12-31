package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.nikolas;

import com.gmail.andrewandy.ascendancy.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendancy.lib.game.data.game.ChallengerDataImpl;
import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.ChallengerModule;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class Nikolas extends AbstractChallenger {

    @Inject
    Nikolas(final NikolasComponentFactory componentFactory) {
        super("Nikolas", challenger -> abilities(challenger, componentFactory), challenger -> runes(challenger, componentFactory),
                ChallengerModule.getLoreOf("Nikolas"));
    }

    private static Ability[] abilities(final Challenger challenger, final NikolasComponentFactory componentFactory) {
        return new Ability[]{componentFactory.createInstrumentOfJudgementFor(challenger)};
    }

    private static PlayerSpecificRune[] runes(final Challenger challenger, final NikolasComponentFactory componentFactory) {
        return new PlayerSpecificRune[0];
    }

    @Override
    public @NotNull IChallengerData toData() {
        try {
            return new ChallengerDataImpl(getName(), new File("Path to file on server"), getLore());
        } catch (final IOException ex) {
            throw new IllegalStateException("Unable to create ChampionData", ex);
        }
    }

}
