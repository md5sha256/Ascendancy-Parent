package com.gmail.andrewandy.ascendency.lib.packet.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public final class CommonUtils {

    public static String capitalise(String string) {
        return Objects.requireNonNull(string).substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static byte[] readFromStream(InputStream src) throws IOException {
        Objects.requireNonNull(src);
        byte[] data = new byte[src.available()];
        int index = 0;
        while (src.available() > 0) {
            data[index++] = (byte) src.read();
        }
        return data;
    }

    public static void reverseArray(byte[] array) {
        int remain = array.length % 2;
        int len = array.length / 2 + remain;
        for (int index = 0; index < len; index++) {
            byte first = array[index++];
            byte back = array[array.length - index];
            array[index] = back;
            array[array.length - index] = first;
        }
    }

}
