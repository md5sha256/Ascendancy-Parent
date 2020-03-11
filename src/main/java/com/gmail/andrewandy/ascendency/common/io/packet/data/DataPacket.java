package com.gmail.andrewandy.ascendency.common.io.packet.data;

import com.gmail.andrewandy.ascendency.common.io.packet.AscendencyPacket;
import com.gmail.andrewandy.ascendency.common.util.Common;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Represents a packet which holds a larger quantity of data (Such as the contents of a file)
 */
public abstract class DataPacket extends AscendencyPacket {

    private byte[] data;

    public DataPacket() {

    }

    public DataPacket(byte[] data) {
        this.data = data;
    }

    public DataPacket(InputStream src) throws IOException {
        data = Common.readFromStream(src);
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
