package com.gmail.andrewandy.ascendency.client.util;

import com.gmail.andrewandy.ascendency.lib.packet.data.FileDataPacket;
import com.gmail.andrewandy.ascendency.lib.packet.data.FileRequestPacket;
import net.minecraftforge.common.ForgeHooks;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FileUtil {

    public static byte[] requestForFile(String fileName, long waitTime, TimeUnit timeUnit) {
        Objects.requireNonNull(fileName);
        Objects.requireNonNull(timeUnit);
        waitTime = waitTime < 0 ? -1 : waitTime;
        FileRequestPacket fileRequestPacket = new FileRequestPacket(ForgeHooks.getCraftingPlayer().getPersistentID(), fileName);
        //TODO;
        FileDataPacket packet = new FileDataPacket(); //TODO change to get from msg channel.
        return packet.getData();
    }

}
