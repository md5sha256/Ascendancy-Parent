package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.breezy;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.breezy.components.AbilityOops;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.breezy.components.AbilityRuneBoom;
import org.jetbrains.annotations.NotNull;

public interface BreezyComponentFactory {

    AbilityOops createOopsFor(@NotNull Challenger challenger);

    AbilityRuneBoom createRuneBoomFor(@NotNull Challenger challenger);

}
