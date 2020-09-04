package com.gmail.andrewandy.ascendency.serverplugin.module;

import co.aikar.taskchain.SpongeTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.CCImmunityManager;
import com.gmail.andrewandy.ascendency.serverplugin.command.AscendancyCommandManager;
import com.gmail.andrewandy.ascendency.serverplugin.configuration.Config;
import com.gmail.andrewandy.ascendency.serverplugin.configuration.YamlConfig;
import com.gmail.andrewandy.ascendency.serverplugin.io.SpongeAscendencyPacketHandler;
import com.gmail.andrewandy.ascendency.serverplugin.items.spell.ISpellManager;
import com.gmail.andrewandy.ascendency.serverplugin.items.spell.SpellManager;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.AscendancyMatch;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.AscendancyMatchService;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.DefaultMatchService;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.MatchFactory;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.draftpick.DraftMatchFactory;
import com.gmail.andrewandy.ascendency.serverplugin.util.game.AscendancyCCManager;
import com.gmail.andrewandy.ascendency.serverplugin.util.game.TickHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import org.jetbrains.annotations.NotNull;

@Singleton public class AscendencyModule extends AbstractModule {

    @NotNull private final AscendencyServerPlugin plugin;

    public AscendencyModule(@NotNull final AscendencyServerPlugin ascendencyServerPlugin) {
        this.plugin = ascendencyServerPlugin;
    }

    @Override protected void configure() {
        bind(TaskChainFactory.class).toInstance(SpongeTaskChainFactory.create(plugin));
        bind(TickHandler.class).asEagerSingleton();
        bind(SpongeAscendencyPacketHandler.class).asEagerSingleton();
        final Config config = new YamlConfig();
        bind(Config.class).toInstance(config);
        bind(new TypeLiteral<MatchFactory<AscendancyMatch>>() {
        }).toInstance(new DraftMatchFactory(config));
        bind(AscendancyMatchService.class).to(DefaultMatchService.class);
        bind(ISpellManager.class).to(SpellManager.class).asEagerSingleton();
        bind(CCImmunityManager.class).to(AscendancyCCManager.class).asEagerSingleton();
        bind(AscendancyCommandManager.class).asEagerSingleton();
    }
}
