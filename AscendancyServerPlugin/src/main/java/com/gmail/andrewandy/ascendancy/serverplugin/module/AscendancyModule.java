package com.gmail.andrewandy.ascendancy.serverplugin.module;

import co.aikar.taskchain.SpongeTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.CCImmunityManager;
import com.gmail.andrewandy.ascendancy.serverplugin.command.AscendancyCommandManager;
import com.gmail.andrewandy.ascendancy.serverplugin.configuration.Config;
import com.gmail.andrewandy.ascendancy.serverplugin.configuration.YamlConfig;
import com.gmail.andrewandy.ascendancy.serverplugin.io.SpongeAscendancyPacketHandler;
import com.gmail.andrewandy.ascendancy.serverplugin.items.spell.ISpellEngine;
import com.gmail.andrewandy.ascendancy.serverplugin.items.spell.SpellEngine;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.AscendancyMatch;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.AscendancyMatchService;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.DefaultMatchService;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.MatchFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.draftpick.DraftMatchFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.gmail.andrewandy.ascendancy.serverplugin.util.game.AscendancyCCManager;
import com.gmail.andrewandy.ascendancy.serverplugin.util.game.TickHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import org.jetbrains.annotations.NotNull;

@Singleton
public class AscendancyModule extends AbstractModule {

    @NotNull
    private final AscendancyServerPlugin plugin;

    public AscendancyModule(@NotNull final AscendancyServerPlugin ascendancyServerPlugin) {
        this.plugin = ascendancyServerPlugin;
    }

    @Override
    protected void configure() {
        requestStaticInjection(Common.class);
        bind(TaskChainFactory.class).toInstance(SpongeTaskChainFactory.create(plugin));
        bind(TickHandler.class).asEagerSingleton();
        bind(SpongeAscendancyPacketHandler.class).asEagerSingleton();
        final Config config = new YamlConfig();
        bind(Config.class).toInstance(config);
        bind(new TypeLiteral<MatchFactory<AscendancyMatch>>() {
        }).toInstance(new DraftMatchFactory(config));
        bind(AscendancyMatchService.class).to(DefaultMatchService.class);
        bind(ISpellEngine.class).to(SpellEngine.class).asEagerSingleton();
        bind(CCImmunityManager.class).to(AscendancyCCManager.class).asEagerSingleton();
        bind(AscendancyCommandManager.class).asEagerSingleton();
    }
}
