package com.gmail.andrewandy.ascendancy.lib.game.data.game;

import com.gmail.andrewandy.ascendancy.lib.data.DataPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.UUID;

public final class ChallengerDataMarkerPacket extends DataPacket {

    private int totalPackets;
    private UUID uuid;

    public ChallengerDataMarkerPacket() {
        this(UUID.randomUUID(), 0);
    }

    public ChallengerDataMarkerPacket(ChallengerDataMarkerPacket other) {
        this(other.uuid, other.totalPackets);
    }

    public ChallengerDataMarkerPacket(UUID uuid, int totalPackets) {
        this.uuid = uuid;
        this.totalPackets = totalPackets;
    }

    public ChallengerDataMarkerPacket(int totalPackets) {
        this.totalPackets = totalPackets;
    }

    private void updateData() {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        if (uuid == null) {
            return;
        }
        buf.writeInt(totalPackets)
                .writeLong(uuid.getLeastSignificantBits())
                .writeLong(uuid.getMostSignificantBits());
        setData(buf.array());
    }

    @Override
    public byte[] getFormattedData() {
        updateData();
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        int len = getIdentifier().getBytes().length;
        buf.writeInt(len).writeBytes(getIdentifier().getBytes());
        len = getData().length;
        if (len > 1) {
            buf.writeInt(len).writeBytes(getData());
        }
        return buf.array();
    }

    @Override
    public int fromBytes(byte[] bytes) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        int len = buf.readInt();
        buf = buf.readBytes(len);
        len = buf.readInt();
        if (len > 1) {
            buf = buf.readBytes(len);
            byte[] data = buf.slice().array();
            ByteBuf dataBuf = ByteBufAllocator.DEFAULT.buffer(data.length).writeBytes(data);
            long least = dataBuf.readLong(), most = dataBuf.readLong();
            this.uuid = new UUID(least, most);
        }
        this.totalPackets = buf.writeBytes(bytes).readInt();
        return buf.readerIndex();
    }

    @Override
    public String getIdentifier() {
        return ChallengerDataMarkerPacket.class.getCanonicalName();
    }

    public UUID getUuid() {
        return uuid;
    }


}
