package com.gmail.andrewandy.ascendancy.lib.data;

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

    public FileRequestPacket(final UUID player) {
        super(player);
    }

    public FileRequestPacket(final FileRequestPacket other) {
        if (other == null) {
            return;
        }
        if (other.filePath != null) {
            this.filePath = Paths.get(other.filePath.toString());
        }
        super.setTargetPlayer(other.getTargetPlayer());
    }

    public FileRequestPacket(final UUID player, final String filePath) {
        this(player, Paths.get(filePath));
    }

    public FileRequestPacket(final UUID player, final Path path) {
        this(player);
        this.filePath = Objects.requireNonNull(path);
    }

    public Path getFilePath() {
        return filePath;
    }

    @Override
    public int fromBytes(final byte[] bytes) {
        final ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytes.length);
        buf.writeBytes(bytes);
        final byte[] rawString = buf.array();
        final String str = new String(rawString);
        final String[] split = str.split(SPLITTER);
        if (split.length < 3) {
            throw new IllegalArgumentException("Invalid buffer parsed!");
        }
        int index = 0;
        final String classAsString = split[index++];
        final String protocolVersion = split[index++];
        final String filePath = split[index];
        try {
            final Class<?> clazz = Class.forName(classAsString);
            if (!FileRequestPacket.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Invalid packet, not type of FileRequestPacket!");
            }
            if (!protocolVersion.equals(PROTOCOL_VERSION)) {
                //Convert.
            }
            this.filePath = Paths.get(filePath);
        } catch (final ClassNotFoundException ex) {
            throw new IllegalArgumentException("Invalid packet, not type of FileRequestPacket!", ex);
        }
        return buf.readerIndex();
    }

    @Override
    public byte[] getFormattedData() {
        final String str = getClass().getCanonicalName() + SPLITTER + PROTOCOL_VERSION + SPLITTER + filePath.toString();
        return str.getBytes();
    }

    @Override
    public String getIdentifier() {
        return FileRequestPacket.class.getCanonicalName() + SPLITTER + PROTOCOL_VERSION;
    }
}
