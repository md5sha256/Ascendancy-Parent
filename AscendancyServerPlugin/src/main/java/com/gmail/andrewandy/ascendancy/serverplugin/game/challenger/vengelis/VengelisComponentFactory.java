package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.vengelis;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.vengelis.components.AbilityGyration;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.vengelis.components.AbilityHauntingFury;
import org.jetbrains.annotations.NotNull;

public interface VengelisComponentFactory {

    @NotNull AbilityGyration createGyrationFor(@NotNull Challenger challenger);

    @NotNull AbilityHauntingFury createHauntingFuryFor(@NotNull Challenger challenger);

}
