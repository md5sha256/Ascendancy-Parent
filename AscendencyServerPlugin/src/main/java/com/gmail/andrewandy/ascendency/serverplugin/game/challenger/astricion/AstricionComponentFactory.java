package com.gmail.andrewandy.ascendency.serverplugin.game.challenger.astricion;

import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.Challenger;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.astricion.components.AbilityDemonicCapacity;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.astricion.components.AbilitySuppression;
import org.jetbrains.annotations.NotNull;

public interface AstricionComponentFactory {

    // Astricion
    AbilityDemonicCapacity createDemonicCapacityFor(@NotNull Challenger toBind);

    // Astricion
    AbilitySuppression createSuppressionFor(@NotNull Challenger toBind);


}
