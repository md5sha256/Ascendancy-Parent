package com.gmail.andrewandy.ascendency.serverplugin.game.challenger.vengelis;

import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.vengelis.components.AbilityGyration;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.vengelis.components.AbilityHauntingFury;
import org.jetbrains.annotations.NotNull;

public interface VengelisComponentFactory {

    AbilityGyration createGyrationFor(@NotNull Challenger challenger);

    AbilityHauntingFury createHauntingFuryFor(@NotNull Challenger challenger);

}
