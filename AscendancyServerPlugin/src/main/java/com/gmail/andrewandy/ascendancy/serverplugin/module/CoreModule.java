package com.gmail.andrewandy.ascendancy.serverplugin.module;

import com.gmail.andrewandy.ascendancy.serverplugin.api.challenger.CCImmunityManager;
import com.gmail.andrewandy.ascendancy.serverplugin.api.mechanics.AscendancyDamageUtil;
import com.gmail.andrewandy.ascendancy.serverplugin.items.spell.ISpellEngine;
import com.gmail.andrewandy.ascendancy.serverplugin.items.spell.SpellEngine;
import com.gmail.andrewandy.ascendancy.serverplugin.listener.AttributeInitializer;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.AscendancyMatchFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.draftpick.DraftMatchFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.util.game.AscendancyCCManager;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Module which injects services for core-features such as {@link AscendancyMatchFactory} and
 * {@link ISpellEngine}
 * <p>
 * This module should <strong>always</strong> be initialized <i>after</i>> {@link AscendancySpongeModule} or its equivalent.
 * </p>
 *
 * @see AscendancySpongeModule
 */
@Singleton
public class CoreModule extends AbstractModule {

    public CoreModule() {
    }

    @Override
    protected void configure() {
        bind(AscendancyDamageUtil.class).asEagerSingleton();
        bind(AscendancyMatchFactory.class).to(DraftMatchFactory.class);

        // Does have a listener, however we can workaround
        bind(ISpellEngine.class).to(SpellEngine.class).asEagerSingleton();

        bind(CCImmunityManager.class).to(AscendancyCCManager.class).asEagerSingleton();
        bind(AttributeInitializer.class).asEagerSingleton();
    }
}
