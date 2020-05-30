package com.gmail.andrewandy.ascendency.serverplugin;

import com.gmail.andrewandy.ascendency.lib.util.CommonUtils;
import com.gmail.andrewandy.ascendency.serverplugin.game.challenger.Season1Challengers;
import com.gmail.andrewandy.ascendency.serverplugin.io.SpongeAscendencyPacketHandler;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.MatchMakingService;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.draftpick.DraftPickMatch;
import com.gmail.andrewandy.ascendency.serverplugin.matchmaking.match.SimplePlayerMatchManager;
import com.gmail.andrewandy.ascendency.serverplugin.util.Common;
import com.gmail.andrewandy.ascendency.serverplugin.util.ForceLoadChunks;
import com.gmail.andrewandy.ascendency.serverplugin.util.YamlLoader;
import com.gmail.andrewandy.ascendency.serverplugin.util.keybind.ActiveKeyHandler;
import com.google.inject.Inject;
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
import java.util.logging.Level;

@Plugin(id = "ascendencyserverplugin", name = "AscendencyServerPlugin", version = "${project.version}", description = "Ascendency Server Plugin", authors = {
    "andrewandy"}) public class AscendencyServerPlugin {

    private static final String DEFAULT_NETWORK_CHANNEL_NAME = "ASCENDENCY_DEFAULT_CHANNEL";
    private static AscendencyServerPlugin instance;
    private MatchMakingService<DraftPickMatch> matchMatchMakingService;


    @Inject @ConfigDir(sharedRoot = true) private File dataFolder;
    private YAMLConfigurationLoader configurationLoader;

    @Inject private Logger logger;

    @Inject public AscendencyServerPlugin() {
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

    public Logger getLogger() {
        return logger;
    }

    @Listener(order = Order.DEFAULT) public void onServerStart(final GameStartedServerEvent event) {
        instance = this;
        final String load = Season1Challengers.LOAD; //Load up S1 champions.
        Common.setup();
        Common.setPrefix("[CustomServerMod]");
        loadSettings();
        setupIO();
        loadKeybindHandlers();
        ForceLoadChunks.getInstance().loadSettings(); //Register the force event handler.
        SimplePlayerMatchManager.enableManager();
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
        SpongeAscendencyPacketHandler.getInstance().initSponge();
        Common.log(Level.INFO, "Loading complete.");
    }


    private void unregisterIO() {
        Common.log(Level.INFO, "Disabling Network Channel...");
        SpongeAscendencyPacketHandler.getInstance().disable();
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
        final MatchMakingService.MatchMakingMode mode =
            MatchMakingService.MatchMakingMode.valueOf(modeEnumName);
        final MatchMakingService<DraftPickMatch> service =
            new MatchMakingService<>(min, max, () -> new DraftPickMatch(Math.round(max / 2f)))
                .setMatchMakingMode(mode);
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
