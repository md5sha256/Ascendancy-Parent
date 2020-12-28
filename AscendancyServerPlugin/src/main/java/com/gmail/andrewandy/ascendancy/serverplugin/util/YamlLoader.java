package com.gmail.andrewandy.ascendancy.serverplugin.util;

import com.gmail.andrewandy.ascendancy.serverplugin.AscendancyServerPlugin;
import com.gmail.andrewandy.ascendancy.lib.util.CommonUtils;
import com.google.inject.Inject;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * A helper class for loading yaml files from the jar.
 * This class is NOT thread-safe; However, it is safe to
 * instantiate new instances of this class away from the "Main" server thread.
 */
public class YamlLoader {

    @Inject
    private static AscendancyServerPlugin plugin;
    private YAMLConfigurationLoader loader;

    private YamlLoader() {
    }

    /**
     * Attempts to load a YAML file from this jar.
     *
     * @param fileName The name of the file.
     * @throws IllegalArgumentException if no file was found in this jar with the given name.
     */
    public YamlLoader(@NotNull final String fileName) {
        final File folder = plugin.getDataFolder();
        final File file = new File(folder.getAbsolutePath(), fileName);
        final OutputStream os;
        try (final InputStream inputStream = YamlLoader.class.getClassLoader()
                .getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException(
                        "Null InputStream - No file found in the jar with name " + fileName);
            }
            if (!file.isFile()) {
                file.createNewFile();
            }
            os = new FileOutputStream(file);
            os.write(CommonUtils.readFromStream(inputStream));
            os.close();
        } catch (final IOException ex) {
            throw new IllegalStateException("Unnable to load or copy file from jar!", ex);
        }
        loader = YAMLConfigurationLoader.builder().setFile(file).build();
    }

    /**
     * Get a {@link YAMLConfigurationLoader} of this file.
     *
     * @return Returns a YAMLConfiguration loader of this file.
     */
    @NotNull
    public YAMLConfigurationLoader getLoader() {
        return loader;
    }

}
