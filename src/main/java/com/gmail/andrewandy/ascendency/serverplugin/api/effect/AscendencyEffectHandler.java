package com.gmail.andrewandy.ascendency.serverplugin.api.effect;

import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import com.google.inject.Inject;

public enum AscendencyEffectHandler {

    INSTANCE;

    @Inject private static AscendencyServerPlugin plugin;

    public void registerHandlers() {
        RootEffect.registerHandler(plugin);
    }

}
