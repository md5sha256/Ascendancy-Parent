package com.gmail.andrewandy.ascendancy.lib.keybind;

import com.gmail.andrewandy.ascendancy.lib.AscendancyPacket;
import com.gmail.andrewandy.ascendancy.lib.data.FileDataPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.UUID;

public class CustomKeyPressedPacket extends AscendancyPacket {

    private static final String VERSION = "0";

    private KeyPressAction pressAction;
    private AscendancyKey key;

    public CustomKeyPressedPacket() {

    }

    public CustomKeyPressedPacket(UUID player, KeyPressAction action) {
        super(player);
        this.pressAction = action;
    }

    public CustomKeyPressedPacket(UUID player, KeyPressAction action, AscendancyKey key) {
        this(player, action);
        this.key = key;
    }

    @Override
    public byte[] getFormattedData() {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(Integer.BYTES);
        String identifier = getIdentifier();
        byte[] identifierBytes = identifier.getBytes();
        buf.writeInt(identifierBytes.length)
                .writeBytes(identifierBytes)
                .writeInt(pressAction == null ? -1 : pressAction.ordinal())
                .writeInt(key == null ? -1 : key.ordinal());
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
        int actionOrdinal = buf.readInt();
        pressAction = actionOrdinal > 0 ? KeyPressAction.values()[actionOrdinal] : null;
        int keyOrdinal = buf.readInt();
        key = keyOrdinal > 0 ? AscendancyKey.values()[keyOrdinal] : null;
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
