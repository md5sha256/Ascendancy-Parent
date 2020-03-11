package com.gmail.andrewandy.ascendency.common.util;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;

/**
 * Contains utility methods which are common to both the client and the server.
 * //TODO Migrate Client-Only methods to the client side and server only to server side.
 */
public class Common {

    private static String prefix = "";

    public static void setPrefix(String prefix) {
        Common.prefix = prefix;
    }

    public static void tell(EntityPlayer player, String... messages) {
        Objects.requireNonNull(player);
        for (String s : messages) {
            player.sendMessage(new TextComponentString(colourise(s)));
        }
    }

    public static void tell(Collection<EntityPlayer> players, String... messages) {
        for (EntityPlayer player : players) {
            tell(player, messages);
        }
    }

    public static String colourise(String message) {
        char code = '&';

        int index = message.indexOf(code);
        while (index != -1) {
            index = message.indexOf(code);
            if (index == message.length() - 1) {
                break;
            }
            char after = message.charAt(index + 1);
            ChatFormatting colour = ChatFormatting.getByChar(after);
            if (colour != null) {
                //Replace the message with the Minecraft chat colour.
                message = message.replaceAll(code + String.valueOf(after), colour.toString());
            }
        }
        return message;
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
