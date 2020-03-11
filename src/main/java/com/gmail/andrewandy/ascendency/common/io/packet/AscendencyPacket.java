package com.gmail.andrewandy.ascendency.common.io.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.spongepowered.api.network.ChannelBuf;

/**
 * Represents a packet from Ascendency
 */
public abstract class AscendencyPacket implements IMessage {

    public abstract byte[] getFormattedData();

    /**
     * Read the data from a byte array. Equivalent to {@link #fromBytes(ByteBuf) and {@link #readFrom(ChannelBuf)}}
     *
     * @param bytes the data.
     * @return The new position we have read to.
     */
    public abstract int fromBytes(byte[] bytes);

    @Override
    public void fromBytes(ByteBuf buf) {
        int change = fromBytes(buf.array());
        buf.readBytes(change);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBytes(getFormattedData());
    }

    public abstract String getIdentifier();
}
