package com.gmail.andrewandy.ascendency.serverplugin.util;

import com.gmail.andrewandy.ascendency.lib.packet.util.CommonUtils;
import com.gmail.andrewandy.ascendency.serverplugin.AscendencyServerPlugin;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.*;

/**
 * A helper class for loading yaml files from the jar.
 * This class is NOT thread-safe.
 */
public class YamlLoader {

    private YAMLConfigurationLoader loader;

    private YamlLoader() {
    }

    /**
     * Attempts to load a YAML file from this jar.
     *
     * @param fileName The name of the file.
     * @throws IllegalArgumentException if no file was found in this jar with the given name.
     */
    public YamlLoader(String fileName) {
        File folder = AscendencyServerPlugin.getInstance().getDataFolder();
        File file = new File(folder.getAbsolutePath(), fileName);
        OutputStream os;
        try (InputStream inputStream = YamlLoader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Null InputStream - No file found in the jar with name " + fileName);
            }
            if (!file.isFile()) {
                file.createNewFile();
            }
            os = new FileOutputStream(file);
            os.write(CommonUtils.readFromStream(inputStream));
            os.close();
        } catch (IOException ex) {
            throw new IllegalStateException("Unnable to load or copy file from jar!", ex);
        }
        loader = YAMLConfigurationLoader.builder().setFile(file).build();
    }

    /**
     * Get a {@link YAMLConfigurationLoader} of this file.
     *
     * @return Returns a YAMLConfiguration loader of this file.
     */
    public YAMLConfigurationLoader getLoader() {
        return loader;
    }

}
