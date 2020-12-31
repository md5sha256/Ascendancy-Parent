package com.gmail.andrewandy.ascendancy.serverplugin.game.challenger;

import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.astricion.Astricion;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.astricion.AstricionComponentFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.Bella;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.bella.BellaComponentFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.breezy.Breezy;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.breezy.BreezyComponentFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.Knavis;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.knavis.KnavisComponentFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.nikolas.Nikolas;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.nikolas.NikolasComponentFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.vengelis.Vengelis;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.vengelis.VengelisComponentFactory;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class ChallengerModule extends AbstractModule {

    public static List<String> getLoreOf(@NotNull final String name) {
        // FIXME Implementation missing!
        return Collections.emptyList();
    }

    @Override
    protected void configure() {

        install(new FactoryModuleBuilder().build(AstricionComponentFactory.class));
        install(new FactoryModuleBuilder().build(BellaComponentFactory.class));
        install(new FactoryModuleBuilder().build(BreezyComponentFactory.class));
        install(new FactoryModuleBuilder().build(KnavisComponentFactory.class));
        install(new FactoryModuleBuilder().build(NikolasComponentFactory.class));
        install(new FactoryModuleBuilder().build(VengelisComponentFactory.class));

        bind(Astricion.class).asEagerSingleton();
        bind(Bella.class).asEagerSingleton();
        requestStaticInjection(Bella.class);
        bind(Breezy.class).asEagerSingleton();
        bind(Knavis.class).asEagerSingleton();
        bind(Nikolas.class).asEagerSingleton();
        bind(Vengelis.class).asEagerSingleton(); // Vengelis depends on the instance of Knavis!

    }

}
