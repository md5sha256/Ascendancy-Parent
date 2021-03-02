package com.gmail.andrewandy.ascendancy.serverplugin.configuration;

import co.aikar.taskchain.TaskChainFactory;
import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.gmail.andrewandy.ascendancy.serverplugin.util.Common;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class YamlConfig implements Config {

    private final Path path;
    private YamlConfigurationLoader loader;
    private ConfigurationNode cachedNode;
    @Inject
    private TaskChainFactory taskChainFactory;

    public YamlConfig(@Assisted final Path path) {
        this.path = path;
    }

    @Override
    public @NotNull ConfigurationNode getRootNode() {
        // Lazy init
        if (cachedNode == null) {
            try {
                load();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return cachedNode;
    }

    @Override
    public void load() throws IOException {
        Common.log(Level.INFO, "&bLoading settings from disk...");
        this.loader = YamlConfigurationLoader.builder().path(path).defaultOptions(ConfigurationOptions.defaults()).build();
        final File file = path.toFile();
        final String fileName = file.getName();
        final URL url = AscendancyServerPlugin.class.getClassLoader().getResource(fileName);
        final YamlConfigurationLoader defaults = YamlConfigurationLoader.builder().url(url).build();
        final ConfigurationNode defaultRoot = defaults.load();
        final long time = System.currentTimeMillis();
        this.cachedNode = loader.load();
        this.cachedNode.mergeFrom(defaultRoot);
        Common.log(
                Level.INFO,
                "&aLoad complete! Took " + (System.currentTimeMillis() - time) + "ms."
        );
    }

    public void save() throws IOException {
        loader.save(cachedNode);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveAsync() {
        final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        final ConfigurationNode copy = cachedNode.copy();
        taskChainFactory.newChain().async(() -> {
            try {
                loader.save(copy);
                completableFuture.complete(null);
            } catch (IOException ex) {
                if (!completableFuture.isDone()) {
                    completableFuture.completeExceptionally(ex);
                }
            }
        });
        return completableFuture;
    }

}
