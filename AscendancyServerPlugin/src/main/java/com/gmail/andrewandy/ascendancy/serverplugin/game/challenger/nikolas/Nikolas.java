package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.nikolas;

import com.gmail.andrewandy.ascendancy.lib.game.data.IChallengerData;
import com.gmail.andrewandy.ascendancy.lib.game.data.game.ChallengerDataImpl;
import com.gmail.andrewandy.ascendancy.serverplugin.api.ability.Ability;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.AbstractChallenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.api.rune.PlayerSpecificRune;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.ChallengerModule;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class Nikolas extends AbstractChallenger {

    Nikolas() {
        super("Nikolas", challenger -> abilities(challenger), challenger -> runes(challenger), ChallengerModule.getLoreOf("Nikolas"));
    }

    private static Ability[] abilities(final Challenger challenger) {
        return new Ability[0];
    }

    private static PlayerSpecificRune[] runes(final Challenger challenger) {
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
