package com.gmail.andrewandy.ascendancy.serverplugin;

import com.gmail.andrewandy.ascendancy.lib.util.CommonUtils;
import com.gmail.andrewandy.ascendancy.serverplugin.configuration.Config;
import com.gmail.andrewandy.ascendancy.serverplugin.game.challenger.ChallengerModule;
import com.gmail.andrewandy.ascendancy.serverplugin.io.SpongeAscendancyPacketHandler;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.DefaultMatchService;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.IMatchMakingService;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.draftpick.DraftMatchFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.PlayerMatchManager;
import com.gmail.andrewandy.ascendancy.serverplugin.matchmaking.match.SimplePlayerMatchManager;
import com.gmail.andrewandy.ascendancy.serverplugin.module.AscendancyModule;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.gmail.andrewandy.ascendancy.serverplugin.util.CustomEvents;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Listeners;
import com.gmail.andrewandy.ascendancy.serverplugin.util.YamlLoader;
import com.gmail.andrewandy.ascendancy.serverplugin.util.keybind.KeyBindHandler;
import com.google.inject.Inject;
import com.google.inject.Injector;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;

@Plugin(id = "ascendancyserverplugin",
        name = "AscendancyServerPlugin",
        version = "0.1-ALPHA",
        description = "Ascendancy Server Plugin",
        authors = {"andrewandy", "resonabit"})

public class AscendancyServerPlugin {

    private static final String DEFAULT_NETWORK_CHANNEL_NAME = "ASCENDANCY_DEFAULT_CHANNEL";
    private static Injector injector;
    private final AscendancyModule ascModule;
    private final ChallengerModule challengerModule = new ChallengerModule();
    private DefaultMatchService matchMatchMakingService;
    private KeyBindHandler keyBindHandler;
    @Inject
    @ConfigDir(sharedRoot = true)
    private File dataFolder;
    private YAMLConfigurationLoader configurationLoader;
    @Inject
    private Logger logger;
    @Inject
    private Injector parent;
    private Config config;

    @Inject
    public AscendancyServerPlugin() {
        ascModule = new AscendancyModule(this);
    }

    public File getDataFolder() {
        return dataFolder;
    }

    @NotNull
    public ConfigurationNode getSettings() {
        try {
            return configurationLoader.load();
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public Logger getLogger() {
        return logger;
    }

    @Listener(order = Order.DEFAULT)
    public void onServerStart(final GameStartedServerEvent event) {
        injector = parent.createChildInjector(ascModule, challengerModule);
        Common.setPrefix("[CustomServerMod]");
        loadSettings();
        config = injector.getInstance(Config.class);
        setupIO();
        loadKeybindHandlers();
        PlayerMatchManager matchManager = injector.getInstance(PlayerMatchManager.class);
        ((SimplePlayerMatchManager) matchManager).enableManager();
        Sponge.getEventManager().registerListeners(this, CustomEvents.INSTANCE);
        Sponge.getEventManager().registerListeners(this, new Listeners());
        loadMatchMaking(); //Load after the player match manager.
        Common.log(Level.INFO, "Plugin enabled!");
    }

    @Listener(order = Order.DEFAULT)
    public void onServerStop(final GameStoppedServerEvent event) {
        ((SimplePlayerMatchManager) injector.getInstance(PlayerMatchManager.class))
                .disableManager();
        unregisterIO();
        unregisterKeybindHandlers();
        Common.log(Level.INFO, "Goodbye! Plugin has been disabled.");
        if (matchMatchMakingService != null) {
            matchMatchMakingService.clearQueue();
        }
    }

    @Listener(order = Order.DEFAULT)
    public void onServerReload(final GameReloadEvent event) {
        loadSettings();
    }


    public void loadSettings() {
        Common.log(Level.INFO, "&bLoading settings from disk...");
        final long time = System.currentTimeMillis();
        configurationLoader = new YamlLoader("settings.yml").getLoader();
        Common.log(Level.INFO,
                "&aLoad complete! Took " + (System.currentTimeMillis() - time) + "ms.");
        try {
            injector.getInstance(Config.class).loadFromFile(Paths.get(
                    getDataFolder().getAbsolutePath().concat(File.separator).concat("Config.yml")));
        } catch (final IOException e) {
            Common.log(Level.SEVERE, "&cUnable to load config!");
            e.printStackTrace();
        }
    }

    private void loadKeybindHandlers() {
        Common.log(Level.INFO, "&b[Key Binds] Loading active key handler.");
        if (this.keyBindHandler != null) {
            unregisterKeybindHandlers();
        }
        this.keyBindHandler = injector.getInstance(KeyBindHandler.class);
        Sponge.getEventManager().registerListeners(this, keyBindHandler);
    }

    private void unregisterKeybindHandlers() {
        Sponge.getEventManager().unregisterListeners(keyBindHandler);
    }

    private void setupIO() {
        Common.log(Level.INFO, "Loading Network Channel...");
        injector.getInstance(SpongeAscendancyPacketHandler.class).initSponge();
        Common.log(Level.INFO, "Loading complete.");
    }


    private void unregisterIO() {
        Common.log(Level.INFO, "Disabling Network Channel...");
        injector.getInstance(SpongeAscendancyPacketHandler.class).disable();
        Common.log(Level.INFO, "Network channel disabled.");
    }

    private void loadMatchMaking() throws IllegalArgumentException {
        ConfigurationNode node = getSettings();
        node = node.getNode("MatchMaking");
        if (node == null) {
            throw new IllegalArgumentException(
                    "Invalid settings detected! Missing MatchMaking section!");
        }
        final int min = node.getNode("Min-Players").getInt();
        final int max = node.getNode("Max-Players").getInt();
        final String modeEnumName = node.getNode("Mode").getString().toUpperCase();
        final IMatchMakingService.MatchMakingMode mode =
                IMatchMakingService.MatchMakingMode.valueOf(modeEnumName);
        final DefaultMatchService service =
                new DefaultMatchService(new DraftMatchFactory(config), config).setMatchMakingMode(mode);
        if (matchMatchMakingService != null) {
            for (final Player player : matchMatchMakingService.clearQueue()) {
                service.addToQueue(player);
            }
        }
        Common.log(Level.INFO,
                "7a[Matchmaking] Loaded: Max-Players = " + max + ", Min-Players = " + min
                        + ", Mode = " + CommonUtils.capitalise(mode.name().toLowerCase()));
        matchMatchMakingService = service;
    }
}
