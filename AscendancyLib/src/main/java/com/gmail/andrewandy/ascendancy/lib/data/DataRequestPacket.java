package com.gmail.andrewandy.ascendancy.lib.data;

import com.gmail.andrewandy.ascendancy.lib.AscendancyPacket;

import java.util.UUID;

public abstract class DataRequestPacket extends AscendancyPacket {

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
