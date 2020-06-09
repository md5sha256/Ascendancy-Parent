package com.gmail.andrewandy.ascendency.serverplugin.module;

import co.aikar.taskchain.SpongeTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.CCImmunityManager;
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
import com.google.inject.*;
import org.jetbrains.annotations.NotNull;

@Singleton public class AscendencyModule extends AbstractModule {

    @NotNull private final AscendencyServerPlugin plugin;
    private MatchFactory<AscendancyMatch> matchFactory;

    public AscendencyModule(@NotNull final AscendencyServerPlugin ascendencyServerPlugin) {
        this.plugin = ascendencyServerPlugin;
    }

    public void setMatchFactory(final MatchFactory<AscendancyMatch> matchFactory) {
        this.matchFactory = matchFactory;
    }

    @Override protected void configure() {
        bind(SpongeAscendencyPacketHandler.class).toInstance(new SpongeAscendencyPacketHandler());
        bind(AscendencyModule.class).toInstance(this);
        final Config config = new YamlConfig();
        bind(Config.class).toInstance(config);
        matchFactory = new DraftMatchFactory(config);
        bind(new TypeLiteral<MatchFactory<AscendancyMatch>>() {
        }).toInstance(matchFactory);
        bind(AscendancyMatchService.class).to(DefaultMatchService.class);
        bind(ISpellManager.class).toInstance(SpellManager.INSTANCE);
        bind(CCImmunityManager.class).toInstance(AscendancyCCManager.INSTANCE);
        bind(TaskChainFactory.class).toInstance(SpongeTaskChainFactory.create(plugin));
    }
}
