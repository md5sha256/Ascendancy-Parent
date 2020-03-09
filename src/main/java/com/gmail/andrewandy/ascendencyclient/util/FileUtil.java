package com.gmail.andrewandy.ascendencyclient.util;

import com.gmail.andrewandy.ascendencyserverplugin.io.packet.FileRequestPacket;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FileUtil {

    public static byte[] requestForFile(String fileName, long waitTime, TimeUnit timeUnit) {
        Objects.requireNonNull(fileName);
        Objects.requireNonNull(timeUnit);
        waitTime = waitTime < 0 ? -1 : waitTime;
        FileRequestPacket fileRequestPacket = new FileRequestPacket(fileName);
        //TODO
        return new byte[0];
    }

}
