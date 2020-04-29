package com.gmail.andrewandy.ascendency.serverplugin.configuration;

import com.gmail.andrewandy.ascendency.serverplugin.util.Common;
import com.gmail.andrewandy.ascendency.serverplugin.util.YamlLoader;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;

public class YamlConfig implements Config {

    private ConfigurationNode root;
    private YAMLConfigurationLoader loader;

    public YamlConfig() {

    }

    @Override public void loadFromFile(final Path path) throws IOException {
        final YAMLConfigurationLoader loader;
        Common.log(Level.INFO, "&bLoading settings from disk...");
        final long time = System.currentTimeMillis();
        loader = new YamlLoader("settings.yml").getLoader();
        this.loader = loader;
        root = loader.load();
        Common.log(Level.INFO,
            "&aLoad complete! Took " + (System.currentTimeMillis() - time) + "ms.");
    }

    @Override public void save() throws IOException {
        loader.save(root);
    }

    @Override public ConfigurationNode getRootNode() {
        return root;
    }



}
