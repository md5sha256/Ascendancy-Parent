package com.gmail.andrewandy.ascendency.lib.packet.data;

import com.gmail.andrewandy.ascendency.lib.packet.AscendencyPacket;

import java.util.UUID;

public abstract class DataRequestPacket extends AscendencyPacket {

    private byte[] requestMessage;

    public DataRequestPacket() {
    }

    public DataRequestPacket(UUID player) {
        super(player);
    }

    public DataRequestPacket(UUID player, byte[] requestMessage) {
        this(player);
        this.requestMessage = requestMessage;
    }

    public byte[] getRequestMessage() {
        return requestMessage;
    }

    protected void setRequestMessage(byte[] message) {
        this.requestMessage = message;
    }

}
