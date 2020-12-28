package com.gmail.andrewandy.ascendancy.lib.results;

import com.gmail.andrewandy.ascendancy.lib.AscendancyPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.UUID;

public class AscendancyResultPacket extends AscendancyPacket implements ResultPacket {

    public static AscendancyResultPacket SUCCESS = new AscendancyResultPacket("SUCCESS", Result.SUCCESS);
    public static AscendancyResultPacket FAILURE = new AscendancyResultPacket("FAILURE", Result.FAILURE);
    public static AscendancyResultPacket NO_PERMS = new AscendancyResultPacket("NO_PERMS", Result.NO_PERMS);
    private String name;
    private Result result;

    public AscendancyResultPacket() {
    }

    public AscendancyResultPacket(String name) {
        this.name = name;
    }

    public AscendancyResultPacket(String name, Result result) {
        this(name);
        this.result = result;
    }

    private AscendancyResultPacket(AscendancyResultPacket other) {
        this.name = other.name;
        this.result = other.result;
    }

    public AscendancyResultPacket forPlayer(UUID targetPlayer) {
        AscendancyResultPacket resultPacket = new AscendancyResultPacket(this);
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
        return AscendancyResultPacket.class.getCanonicalName();
    }
}
