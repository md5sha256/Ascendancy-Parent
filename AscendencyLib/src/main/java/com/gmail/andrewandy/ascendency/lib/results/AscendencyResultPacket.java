package com.gmail.andrewandy.ascendency.lib.results;

import com.gmail.andrewandy.ascendency.lib.AscendencyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.UUID;

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

    private AscendencyResultPacket(AscendencyResultPacket other) {
        this.name = other.name;
        this.result = other.result;
    }

    public AscendencyResultPacket forPlayer(UUID targetPlayer) {
        AscendencyResultPacket resultPacket = new AscendencyResultPacket(this);
        resultPacket.setTargetPlayer(targetPlayer);
        return resultPacket;
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

    @Override
    public String getIdentifier() {
        return AscendencyResultPacket.class.getCanonicalName();
    }
}
