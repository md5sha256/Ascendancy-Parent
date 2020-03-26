package com.gmail.andrewandy.ascendency.lib.packet.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a packet to request for a file.
 */
public class FileRequestPacket extends DataRequestPacket {

    public static final String PROTOCOL_VERSION = "1";
    private static final String SPLITTER = "::";
    private Path filePath;

    public FileRequestPacket() {

    }

    public FileRequestPacket(UUID player) {
        super(player);
    }

    public FileRequestPacket(FileRequestPacket other) {
        if (other == null) {
            return;
        }
        if (other.filePath != null) {
            this.filePath = Paths.get(other.filePath.toString());
        }
        super.setTargetPlayer(other.getTargetPlayer());
    }

    public FileRequestPacket(UUID player, String filePath) {
        this(player, Paths.get(filePath));
    }

    public FileRequestPacket(UUID player, Path path) {
        this(player);
        this.filePath = Objects.requireNonNull(path);
    }

    public Path getFilePath() {
        return filePath;
    }

    @Override
    public int fromBytes(byte[] bytes) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytes.length);
        buf.writeBytes(bytes);
        byte[] rawString = buf.array();
        String str = new String(rawString);
        String[] split = str.split(SPLITTER);
        if (split.length < 3) {
            throw new IllegalArgumentException("Invalid buffer parsed!");
        }
        int index = 0;
        String classAsString = split[index++];
        String protocolVersion = split[index++];
        String filePath = split[index];
        try {
            Class<?> clazz = Class.forName(classAsString);
            if (!FileRequestPacket.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Invalid packet, not type of FileRequestPacket!");
            }
            if (!protocolVersion.equals(PROTOCOL_VERSION)) {
                //Convert.
            }
            this.filePath = Paths.get(filePath);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Invalid packet, not type of FileRequestPacket!", ex);
        }
        return buf.readerIndex();
    }

    @Override
    public byte[] getFormattedData() {
        String str = getClass().getCanonicalName() + SPLITTER + PROTOCOL_VERSION + SPLITTER + filePath.toString();
        return str.getBytes();
    }

    @Override
    public String getIdentifier() {
        return FileRequestPacket.class.getCanonicalName() + SPLITTER + PROTOCOL_VERSION;
    }
}
