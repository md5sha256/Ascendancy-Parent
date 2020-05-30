package com.gmail.andrewandy.ascendency.lib.data;

import com.gmail.andrewandy.ascendency.lib.AscendencyPacket;

import java.util.UUID;

public abstract class DataRequestPacket extends AscendencyPacket {

    private byte[] requestMessage;

    public DataRequestPacket() {
    }

    public DataRequestPacket(final UUID player) {
        super(player);
    }

    public DataRequestPacket(final UUID player, final byte[] requestMessage) {
        this(player);
        this.requestMessage = requestMessage;
    }

    public byte[] getRequestMessage() {
        return requestMessage;
    }

    protected void setRequestMessage(final byte[] message) {
        this.requestMessage = message;
    }

}
