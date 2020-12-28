package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.components.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface BellaComponentFactory {

    CircletData createCircletData(@NotNull UUID caster, int radius);

    AbilityCircletOfTheAccused createCircletAccusedFor(@NotNull Challenger challenger);

    AbilityReleasedRebellion createReleasedRebellionFor(@NotNull Challenger challenger);

    RuneCoupDEclat createCoupDEclatFor(
            @NotNull AbilityCircletOfTheAccused abilityCircletOfTheAccused);

    RuneDivineCrown createDivineCrownFor(
            @NotNull AbilityCircletOfTheAccused abilityCircletOfTheAccused,
            @NotNull RuneCoupDEclat coupDEclat);

    RuneExpandingAgony createExpandingAgonyFor(
            @NotNull AbilityCircletOfTheAccused abilityCircletOfTheAccused,
            @NotNull RuneCoupDEclat coupDEclat);
}
