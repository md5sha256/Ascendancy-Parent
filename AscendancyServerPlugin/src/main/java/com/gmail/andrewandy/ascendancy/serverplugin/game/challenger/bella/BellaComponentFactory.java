package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.components.AbilityCircletOfTheAccused;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.components.AbilityReleasedRebellion;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.components.CircletData;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.components.RuneCoupDEclat;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.components.RuneDivineCrown;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.components.RuneExpandingAgony;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface BellaComponentFactory {

    CircletData createCircletData(@NotNull UUID caster, int radius);

    AbilityCircletOfTheAccused createCircletAccusedFor(@NotNull Challenger challenger);

    AbilityReleasedRebellion createReleasedRebellionFor(@NotNull Challenger challenger);

    RuneCoupDEclat createCoupDEclatFor(
            @NotNull AbilityCircletOfTheAccused abilityCircletOfTheAccused
    );

    RuneDivineCrown createDivineCrownFor(
            @NotNull AbilityCircletOfTheAccused abilityCircletOfTheAccused,
            @NotNull RuneCoupDEclat coupDEclat
    );

    RuneExpandingAgony createExpandingAgonyFor(
            @NotNull AbilityCircletOfTheAccused abilityCircletOfTheAccused,
            @NotNull RuneCoupDEclat coupDEclat
    );

}
