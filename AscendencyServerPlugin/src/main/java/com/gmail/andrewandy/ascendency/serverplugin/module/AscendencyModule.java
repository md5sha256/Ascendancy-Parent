package com.gmail.andrewandy.ascendency.serverplugin.module;

import com.gmail.andrewandy.ascendency.serverplugin.configuration.Config;
import com.gmail.andrewandy.ascendency.serverplugin.configuration.YamlConfig;
import com.gmail.andrewandy.ascendency.serverplugin.io.SpongeAscendencyPacketHandler;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.AscendancyMatch;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.AscendancyMatchService;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.DefaultMatchService;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.MatchFactory;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.draftpick.DraftMatchFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

@Singleton public class AscendencyModule extends AbstractModule {

    private MatchFactory<AscendancyMatch> matchFactory;

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
    }
}
