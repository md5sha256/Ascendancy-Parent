package com.gmail.andrewandy.ascendancy.serverplugin.module;

import co.aikar.taskchain.SpongeTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.gmail.andrewandy.ascendancy.serverplugin.command.AscendancyCommandManager;
import com.gmail.andrewandy.ascendancy.serverplugin.configuration.Config;
import com.gmail.andrewandy.ascendancy.serverplugin.configuration.YamlConfig;
import com.gmail.andrewandy.ascendancy.serverplugin.io.SpongeAscendancyPacketHandler;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.AscendancyMatchService;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.DefaultMatchService;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.gmail.andrewandy.ascendancy.serverplugin.util.game.TickHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;

/**
 * Module for services which are tightly-coupled to the Sponge API. This module provides utilities such as
 * {@link TickHandler} and {@link SpongeAscendancyPacketHandler}. This module should generally be initialized first
 * as modules such as {@link CoreModule} depend on the utilities provided by this module. On another note, this module
 * requires dependencies such as {@link org.slf4j.Logger} and other dependencies provided by sponge.
 */
@Singleton
public class AscendancySpongeModule extends AbstractModule {

    @Override
    protected void configure() {
        requestStaticInjection(Common.class);
        bind(TickHandler.class).asEagerSingleton();
        bind(SpongeAscendancyPacketHandler.class).asEagerSingleton();
        bind(AscendancyCommandManager.class).asEagerSingleton();
        bind(AscendancyMatchService.class).to(DefaultMatchService.class);
    }

    @Provides
    @Singleton
    public TaskChainFactory provideTaskChainFactory(AscendancyServerPlugin plugin) {
        return SpongeTaskChainFactory.create(plugin);
    }

    @Provides
    @Named("internal-config")
    public Config provideInternalConfig(final AscendancyServerPlugin serverPlugin) {
        return new YamlConfig(new File(serverPlugin.getDataFolder(), "settings.yml").toPath());
    }

    @Provides
    @Named("internal-config")
    public ConfigurationNode provideInternalConfigNode(final Config config) {
        return config.getRootNode();
    }

}
