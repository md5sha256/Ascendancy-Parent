package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.solace;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.solace.components.AbilityCallbackOfTheAfterlife;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.solace.components.AbilityUndiminishedSoul;
import org.jetbrains.annotations.NotNull;

public interface SolaceComponentFactory {

    AbilityCallbackOfTheAfterlife createCallbackOfTheAfterlife(@NotNull final Challenger challenger);

    AbilityUndiminishedSoul createUndiminishedSoul(@NotNull final Challenger challenger);

}
