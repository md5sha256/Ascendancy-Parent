package com.gmail.andrewandy.ascendency.common.io.packet.results;

import com.gmail.andrewandy.ascendencyserverplugin.io.packet.AscendencyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class AscendencyResultPacket extends AscendencyPacket implements ResultPacket {

    public static AscendencyResultPacket SUCCESS = new AscendencyResultPacket("SUCCESS", Result.SUCCESS);
    public static AscendencyResultPacket FAILURE = new AscendencyResultPacket("FAILURE", Result.FAILURE);
    public static AscendencyResultPacket NO_PERMS = new AscendencyResultPacket("NO_PERMS", Result.NO_PERMS);
    private String name;
    private Result result;

    public AscendencyResultPacket() {
    }

    public AscendencyResultPacket(String name) {
        this.name = name;
    }

    public AscendencyResultPacket(String name, Result result) {
        this(name);
        this.result = result;
    }

    public String getName() {
        return name;
    }

    @Override
    public byte[] getFormattedData() {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        byte[] bytes = name.getBytes();
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        buf.writeInt(result.ordinal());
        return buf.array();
    }

    @Override
    public int fromBytes(byte[] bytes) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        buf.writeBytes(bytes);
        int nameLen = buf.readInt();
        this.name = new String(buf.readSlice(nameLen).array());
        int ordinal = buf.readInt();
        this.result = Result.values()[ordinal];
        return buf.readerIndex();
    }


    @Override
    public Result getResult() {
        return result;
    }
}
