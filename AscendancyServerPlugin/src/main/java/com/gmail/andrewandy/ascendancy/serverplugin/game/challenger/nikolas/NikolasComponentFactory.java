package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.nikolas;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.nikolas.components.AbilityInstrumentOfJudgement;
import org.jetbrains.annotations.NotNull;

public interface NikolasComponentFactory {

    AbilityInstrumentOfJudgement createInstrumentOfJudgementFor(@NotNull Challenger challenger);

}
