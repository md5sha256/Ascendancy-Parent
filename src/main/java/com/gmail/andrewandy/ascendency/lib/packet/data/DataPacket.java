package com.gmail.andrewandy.ascendency.lib.packet.data;

import com.gmail.andrewandy.ascendency.lib.packet.AscendencyPacket;
import com.gmail.andrewandy.ascendency.lib.packet.util.CommonUtils;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a packet which holds a larger quantity of data (Such as the contents of a file)
 */
public abstract class DataPacket extends AscendencyPacket {

    private byte[] data;

    public DataPacket() {

    }

    public DataPacket(UUID player) {
        super(player);
    }

    public DataPacket(UUID player, byte[] data) {
        this(player);
        this.data = data;
    }

    public DataPacket(UUID player, InputStream src) throws IOException {
        this(player);
        data = CommonUtils.readFromStream(src);
    }

    public DataPacket(ByteBuf buffer) {
        data = Objects.requireNonNull(buffer).array();
    }


    public DataPacket(ByteBuffer buffer) {
        if (!Objects.requireNonNull(buffer).hasArray()) {
            throw new IllegalArgumentException("Buffer has no array!");
        }
        data = Objects.requireNonNull(buffer).array();
    }

    public byte[] getData() {
        return data;
    }

    protected void setData(byte[] data) {
        this.data = data;
    }

    public void writeToStream(OutputStream outputStream, boolean closeAfter) throws IOException {
        try {
            Objects.requireNonNull(outputStream).write(getFormattedData());
        } finally {
            if (closeAfter && outputStream != null) {
                outputStream.close();
            }
        }
    }
}
