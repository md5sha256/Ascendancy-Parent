package com.gmail.andrewandy.ascendancy.lib;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

/**
 * Represents a packet from Ascendency.
 */
public abstract class AscendancyPacket implements IMessage {

    private transient UUID targetPlayer; //Used to actually send the packet to a player.

    public AscendancyPacket() {
    }

    /**
     * Set the target player to send the packet to. Can be null if its to the server.
     *
     * @param targetPlayer The UUID of the target player, null if its to the server.
     */
    public AscendancyPacket(UUID targetPlayer) {
        this.targetPlayer = targetPlayer;
    }

    public UUID getTargetPlayer() {
        return targetPlayer;
    }

    protected void setTargetPlayer(UUID targetPlayer) {
        this.targetPlayer = targetPlayer;
    }

    /**
     * Get the processed data which can be sent.
     *
     * @return Returns the formatted data.
     */
    public abstract byte[] getFormattedData();

    /**
     * Read the data from a byte array. Equivalent to {@link #fromBytes(ByteBuf);
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

    /**
     * Get the String identifier of this packet.
     *
     * @return Returns the String identifier.
     */
    public abstract String getIdentifier();

}
