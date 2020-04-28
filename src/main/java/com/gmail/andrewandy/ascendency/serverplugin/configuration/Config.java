package com.gmail.andrewandy.ascendency.serverplugin.configuration;

import ninja.leaping.configurate.ConfigurationNode;

import java.io.IOException;
import java.nio.file.Path;

public interface Config {

    void loadFromFile(Path file) throws IOException;

    void save() throws IOException;

    ConfigurationNode getRootNode();



}
