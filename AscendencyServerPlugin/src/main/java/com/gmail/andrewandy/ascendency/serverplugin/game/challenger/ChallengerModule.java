package com.gmail.andrewandy.ascendency.serverplugin.game.challenger;

import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.astricion.Astricion;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.astricion.AstricionComponentFactory;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.astricion.components.AbilityDemonicCapacity;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.astricion.components.AbilitySuppression;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.bella.Bella;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.bella.BellaComponentFactory;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.bella.components.AbilityCircletOfTheAccused;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.bella.components.AbilityReleasedRebellion;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.bella.components.CircletData;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.bella.components.RuneCoupDEclat;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.bella.components.RuneDivineCrown;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.bella.components.RuneExpandingAgony;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class ChallengerModule extends AbstractModule {

    public static List<String> getLoreOf(@NotNull final String name) {
        return Collections.emptyList(); // TODO Implementation
    }

    @Override protected void configure() {

        install(new FactoryModuleBuilder()
                    .implement(AbilityDemonicCapacity.class, AbilityDemonicCapacity.class)
                    .implement(AbilitySuppression.class, AbilitySuppression.class)
                    .build(AstricionComponentFactory.class));

        install(new FactoryModuleBuilder().implement(CircletData.class, CircletData.class)
                    .implement(AbilityCircletOfTheAccused.class, AbilityCircletOfTheAccused.class)
                    .implement(AbilityReleasedRebellion.class, AbilityReleasedRebellion.class)
                    .implement(RuneCoupDEclat.class, RuneCoupDEclat.class)
                    .implement(RuneDivineCrown.class, RuneDivineCrown.class)
                    .implement(RuneExpandingAgony.class, RuneExpandingAgony.class)
                    .build(BellaComponentFactory.class));

        bind(Astricion.class).asEagerSingleton();


        bind(Bella.class).asEagerSingleton();
        requestStaticInjection(Bella.class);

    }

}
