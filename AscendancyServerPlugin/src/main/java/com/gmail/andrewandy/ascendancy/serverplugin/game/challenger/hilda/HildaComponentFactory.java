package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.hilda;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.hilda.components.AbilityIdentityOfPurity;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.hilda.components.AbilityMirrorOfResolution;
import org.jetbrains.annotations.NotNull;

public interface HildaComponentFactory {

    AbilityMirrorOfResolution createMirrorOfResolutionFor(@NotNull Challenger challenger);

    AbilityIdentityOfPurity createIdentityOfPurityFor(@NotNull Challenger challenger);

}
