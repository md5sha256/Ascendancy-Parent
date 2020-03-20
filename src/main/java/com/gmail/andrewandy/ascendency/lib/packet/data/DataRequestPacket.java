package com.gmail.andrewandy.ascendency.lib.packet.data;

import com.gmail.andrewandy.ascendency.lib.packet.AscendencyPacket;

public abstract class DataRequestPacket extends AscendencyPacket {

    private byte[] requestMessage;

    public DataRequestPacket() {
    }

    public DataRequestPacket(byte[] requestMessage) {
        this.requestMessage = requestMessage;
    }

    public byte[] getRequestMessage() {
        return requestMessage;
    }

    protected void setRequestMessage(byte[] message) {
        this.requestMessage = message;
    }

}
