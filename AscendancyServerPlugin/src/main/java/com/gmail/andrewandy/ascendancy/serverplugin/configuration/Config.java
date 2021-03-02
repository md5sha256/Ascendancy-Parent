package com.gmail.andrewandy.ascendancy.serverplugin.configuration;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface Config {

    void load() throws IOException;

    void save() throws IOException;

    @NotNull CompletableFuture<Void> saveAsync();

    @NotNull ConfigurationNode getRootNode();

}
