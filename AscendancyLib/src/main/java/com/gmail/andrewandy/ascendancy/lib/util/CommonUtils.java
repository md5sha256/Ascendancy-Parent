package com.gmail.andrewandy.ascendancy.lib.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.StringJoiner;

public final class CommonUtils {

    public static String capitalise(final String string) {
        return Objects.requireNonNull(string).substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static String titleCase(final String string, final String delimiter) {
        final String[] split = string.split(delimiter);
        if (split.length == 0) {
            return capitalise(string);
        }
        final StringJoiner joiner = new StringJoiner(delimiter);
        for (final String s : split) {
            joiner.add(capitalise(s));
        }
        return joiner.toString();
    }

    @NotNull public static byte[] readFromStream(final InputStream src) throws IOException {
        Objects.requireNonNull(src);
        final byte[] data = new byte[src.available()];
        int index = 0;
        while (src.available() > 0) {
            data[index++] = (byte) src.read();
        }
        return data;
    }

    public static void reverseArray(final byte[] array) {
        final int remain = array.length % 2;
        final int len = array.length / 2 + remain;
        for (int index = 0; index < len; index++) {
            final byte first = array[index++];
            final byte back = array[array.length - index];
            array[index] = back;
            array[array.length - index] = first;
        }
    }

}
