package com.gmail.andrewandy.ascendency.lib.packet.keybind;

import com.gmail.andrewandy.ascendency.lib.packet.AscendencyPacket;
import com.gmail.andrewandy.ascendency.lib.packet.data.FileDataPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class CustomKeyPressedPacket extends AscendencyPacket {

    private static final String VERSION = "0";

    private KeyPressAction pressAction;

    public CustomKeyPressedPacket() {

    }

    public CustomKeyPressedPacket(KeyPressAction action) {
        this.pressAction = action;
    }

    @Override
    public byte[] getFormattedData() {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(Integer.BYTES);
        String identifier = getIdentifier();
        byte[] identifierBytes = identifier.getBytes();
        buf.writeInt(identifierBytes.length)
                .writeBytes(identifierBytes)
                .writeInt(pressAction == null ? -1 : pressAction.ordinal());
        return buf.array();
    }

    @Override
    public int fromBytes(byte[] bytes) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytes.length);
        buf = buf.writeBytes(bytes);
        int len = buf.readInt();
        buf = buf.readBytes(len);
        String identifier = new String(buf.slice().array());
        try {
            Class<?> clazz = Class.forName(identifier.split("::")[0]);
            if (!FileDataPacket.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Packet identifier not type of CustomKeyPressedPacket!");
            }
            String otherVersion = identifier.split("::")[1];
            if (!otherVersion.equals(VERSION)) {
                //Conversion
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Packet identifier not type of FileDataPacket!");
        }
        int ordinal = buf.readInt();
        if (ordinal > 0) {
            pressAction = KeyPressAction.values()[ordinal];
        } else {
            pressAction = null;
        }
        return buf.readerIndex();
    }

    public KeyPressAction getPressAction() {
        return pressAction;
    }

    @Override
    public String getIdentifier() {
        return CustomKeyPressedPacket.class.getCanonicalName() + "::" + VERSION;
    }
}
