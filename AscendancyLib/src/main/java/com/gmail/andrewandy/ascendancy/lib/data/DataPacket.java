package com.gmail.andrewandy.ascendancy.lib.data;

import com.gmail.andrewandy.ascendancy.lib.AscendancyPacket;
import com.gmail.andrewandy.ascendancy.lib.util.CommonUtils;
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
public abstract class DataPacket extends AscendancyPacket {

    private byte[] data;

    public DataPacket() {

    }

    public DataPacket(final UUID player) {
        super(player);
    }

    public DataPacket(final UUID player, final byte[] data) {
        this(player);
        this.data = data;
    }

    public DataPacket(final UUID player, final InputStream src) throws IOException {
        this(player);
        data = CommonUtils.readFromStream(src);
    }

    public DataPacket(final ByteBuf buffer) {
        data = Objects.requireNonNull(buffer).array();
    }


    public DataPacket(final ByteBuffer buffer) {
        if (!Objects.requireNonNull(buffer).hasArray()) {
            throw new IllegalArgumentException("Buffer has no array!");
        }
        data = Objects.requireNonNull(buffer).array();
    }

    public byte[] getData() {
        return data;
    }

    protected void setData(final byte[] data) {
        this.data = data;
    }

    public void writeToStream(final OutputStream outputStream, final boolean closeAfter) throws IOException {
        try {
            Objects.requireNonNull(outputStream).write(getFormattedData());
        } finally {
            if (closeAfter && outputStream != null) {
                outputStream.close();
            }
        }
    }

}
