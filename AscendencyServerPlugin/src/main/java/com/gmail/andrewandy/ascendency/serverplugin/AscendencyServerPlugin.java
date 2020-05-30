package com.gmail.andrewandy.ascendency.serverplugin;

import com.gmail.andrewandy.ascendency.lib.util.CommonUtils;
import com.gmail.andrewandy.ascendency.serverplugin.configuration.Config;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.Challengers;
import com.gmail.andrewandy.ascendency.serverplugin.io.SpongeAscendencyPacketHandler;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.AscendancyMatchFactory;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.DefaultMatchService;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.IMatchMakingService;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.draftpick.DraftMatchFactory;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.SimplePlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.module.AscendencyModule;
import com.gmail.andrewandy.ascendency.serverplugin.util.Common;
import com.gmail.andrewandy.ascendency.serverplugin.util.CustomEvents;
import com.gmail.andrewandy.ascendency.serverplugin.util.ForceLoadChunks;
import com.gmail.andrewandy.ascendency.serverplugin.util.YamlLoader;
import com.gmail.andrewandy.ascendency.serverplugin.util.keybind.ActiveKeyHandler;
import com.gmail.andrewandy.ascendency.serverplugin.util.keybind.KeyBindHandler;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
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

@Plugin(id = "ascendencyserverplugin", name = "AscendencyServerPlugin", version = "0.1-ALPHA", description = "Ascendency Server Plugin", authors = {
    "andrewandy", "resonabit"}) public class AscendencyServerPlugin {

    private static final String DEFAULT_NETWORK_CHANNEL_NAME = "ASCENDENCY_DEFAULT_CHANNEL";
    private static AscendencyServerPlugin instance;
    private static Injector injector;
    private final AscendencyModule module;
    private DefaultMatchService matchMatchMakingService;

    private KeyBindHandler keyBindHandler;


    @Inject @ConfigDir(sharedRoot = true) private File dataFolder;
    private YAMLConfigurationLoader configurationLoader;

    @Inject private Logger logger;
    private Config config;

    @Inject public AscendencyServerPlugin() {
        module = new AscendencyModule();
    }

    public static AscendencyServerPlugin getInstance() {
        return instance;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public ConfigurationNode getSettings() {
        try {
            return configurationLoader.load();
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void setMatchFactory(final AscendancyMatchFactory ascendancyMatchFactory) {
        module.setMatchFactory(ascendancyMatchFactory);
    }

    public Logger getLogger() {
        return logger;
    }

    @Listener(order = Order.DEFAULT) public void onServerStart(final GameStartedServerEvent event) {
        instance = this;
        injector = Guice.createInjector(Stage.PRODUCTION, module);
        final String load = Challengers.LOAD; //Load up S1 champions.
        Common.setup();
        Common.setPrefix("[CustomServerMod]");
        loadSettings();
        setMatchFactory(new DraftMatchFactory(
            injector.getInstance(Config.class))); //Set the match factory here.
        config = injector.getInstance(Config.class);
        setupIO();
        loadKeybindHandlers();
        ForceLoadChunks.getInstance().loadSettings(); //Register the force event handler.
        SimplePlayerMatchManager.enableManager();
        Challengers.initHandlers();
        Sponge.getEventManager().registerListeners(this, CustomEvents.INSTANCE);
        loadMatchMaking(); //Load after the player match manager.
        Common.log(Level.INFO, "Plugin enabled!");
    }

    @Listener(order = Order.DEFAULT) public void onServerStop(final GameStoppedServerEvent event) {
        SimplePlayerMatchManager.disableManager();
        unregisterIO();
        unregisterKeybindHandlers();
        Common.log(Level.INFO, "Goodbye! Plugin has been disabled.");
        if (matchMatchMakingService != null) {
            matchMatchMakingService.clearQueue();
        }
        instance = null;
    }

    @Listener(order = Order.DEFAULT) public void onServerReload(final GameReloadEvent event) {
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
        unregisterKeybindHandlers();
        Sponge.getEventManager().registerListeners(instance, ActiveKeyHandler.INSTANCE);
    }

    private void unregisterKeybindHandlers() {
        Sponge.getEventManager().unregisterListeners(ActiveKeyHandler.INSTANCE);
    }

    private void setupIO() {
        Common.log(Level.INFO, "Loading Network Channel...");
        injector.getInstance(SpongeAscendencyPacketHandler.class).initSponge();
        Common.log(Level.INFO, "Loading complete.");
    }


    private void unregisterIO() {
        Common.log(Level.INFO, "Disabling Network Channel...");
        injector.getInstance(SpongeAscendencyPacketHandler.class).disable();
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
            "7a[Matchmaking] Loaded: Max-Players = " + max + ", Min-Players = " + min + ", Mode = "
                + CommonUtils.capitalise(mode.name().toLowerCase()));
        matchMatchMakingService = service;
    }
}
