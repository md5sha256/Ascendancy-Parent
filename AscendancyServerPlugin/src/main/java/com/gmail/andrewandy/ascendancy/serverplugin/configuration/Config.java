package com.gmail.andrewandy.ascendancy.serverplugin.configuration;

import ninja.leaping.configurate.ConfigurationNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public interface Config {

    void loadFromFile(@NotNull Path file) throws IOException;

    void save() throws IOException;

    ConfigurationNode getRootNode();

}
