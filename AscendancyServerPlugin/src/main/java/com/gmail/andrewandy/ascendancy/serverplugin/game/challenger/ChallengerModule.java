package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger;

import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.astricion.Astricion;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.astricion.AstricionComponentFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.astricion.components.AbilityDemonicCapacity;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.astricion.components.AbilitySuppression;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.Bella;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.BellaComponentFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.components.*;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.breezy.Breezy;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.breezy.BreezyComponentFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.breezy.components.AbilityOops;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.breezy.components.AbilityRuneBoom;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.Knavis;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.KnavisComponentFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.LocationMarkedEvent;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.MarkTeleportationEvent;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.components.*;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.vengelis.Vengelis;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.vengelis.VengelisComponentFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.vengelis.components.AbilityGyration;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.vengelis.components.AbilityHauntingFury;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class ChallengerModule extends AbstractModule {

    public static List<String> getLoreOf(@NotNull final String name) {
        return Collections.emptyList(); // TODO Implementation
    }

    @Override
    protected void configure() {

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

        install(new FactoryModuleBuilder().implement(AbilityOops.class, AbilityOops.class)
                .implement(AbilityRuneBoom.class, AbilityRuneBoom.class)
                .build(BreezyComponentFactory.class));

        install(
                new FactoryModuleBuilder().implement(AbilityLivingGift.class, AbilityLivingGift.class)
                        .implement(AbilityShadowsRetreat.class, AbilityShadowsRetreat.class)
                        .implement(RuneBlessingOfTeleportation.class, RuneBlessingOfTeleportation.class)
                        .implement(RuneChosenOTEarth.class, RuneChosenOTEarth.class)
                        .implement(RuneHeartOfTheDryad.class, RuneHeartOfTheDryad.class)
                        .implement(LocationMarkedEvent.class, LocationMarkedEvent.class)
                        .implement(MarkTeleportationEvent.class, MarkTeleportationEvent.class)
                        .build(KnavisComponentFactory.class));

        install(new FactoryModuleBuilder().implement(AbilityGyration.class, AbilityGyration.class)
                .implement(AbilityHauntingFury.class, AbilityHauntingFury.class)
                .build(VengelisComponentFactory.class));

        bind(Astricion.class).asEagerSingleton();
        bind(Bella.class).asEagerSingleton();
        requestStaticInjection(Bella.class);
        bind(Breezy.class).asEagerSingleton();
        bind(Knavis.class).asEagerSingleton();
        bind(Vengelis.class).asEagerSingleton(); // Vengelis depends on the instance of Knavis!

    }

}
