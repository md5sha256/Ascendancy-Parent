package com.gmail.andrewandy.ascendency.serverplugin.module;

import co.aikar.taskchain.SpongeTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.gmail.andrewandy.ascendency.serverplugin.api.challenger.CCImmunityManager;
import com.gmail.andrewandy.ascendency.serverplugin.configuration.Config;
import com.gmail.andrewandy.ascendency.serverplugin.configuration.YamlConfig;
import com.gmail.andrewandy.ascendency.serverplugin.io.SpongeAscendencyPacketHandler;
import com.gmail.andrewandy.ascendency.serverplugin.items.spell.ISpellManager;
import com.gmail.andrewandy.ascendency.serverplugin.items.spell.Spell;
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
    public AscendencyModule(@NotNull final AscendencyServerPlugin ascendencyServerPlugin) {
        this.plugin = ascendencyServerPlugin;
    }

    @Override protected void configure() {
        bind(SpongeAscendencyPacketHandler.class).in(Singleton.class);
        final Config config = new YamlConfig();
        bind(Config.class).toInstance(config);
        bind(new TypeLiteral<MatchFactory<AscendancyMatch>>() {
        }).toInstance(new DraftMatchFactory(config));
        bind(AscendancyMatchService.class).to(DefaultMatchService.class);
        bind(ISpellManager.class).to(SpellManager.class).in(Singleton.class);
        bind(CCImmunityManager.class).to(AscendancyCCManager.class).in(Singleton.class);
        bind(TaskChainFactory.class).toInstance(SpongeTaskChainFactory.create(plugin));
    }
}
