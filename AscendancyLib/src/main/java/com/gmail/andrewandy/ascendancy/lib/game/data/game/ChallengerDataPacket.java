package com.gmail.andrewandy.ascendancy.lib.game.data.game;

import com.gmail.andrewandy.ascendancy.lib.data.DataPacket;
import com.gmail.andrewandy.ascendancy.lib.game.data.IChallengerData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Packet used to synchronise champion data as the player joins.
 */
public class ChallengerDataPacket extends DataPacket {

    private static final String VERSION = "0";

    public ChallengerDataPacket() {
    }

    public ChallengerDataPacket(UUID player, IChallengerData data) {
        super(player);
        //Write the champion data to bytes.
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        int len = data.getName().getBytes().length;
        byte[] icon = data.getIcon();
        int size = data.getLore().size();
        buf.writeInt(len)
                .writeBytes(data.getName().getBytes())
                .writeInt(icon.length)
                .writeInt(size);
        for (String lore : data.getLore()) {
            byte[] bytes = lore.getBytes();
            buf.writeInt(bytes.length)
                    .writeBytes(bytes);
        }
        super.setData(buf.array());
    }

    public ChallengerDataPacket(IChallengerData data) {
        this(null, data);
    }

    public ChallengerDataPacket(UUID player, byte[] data) {
        super(player, data);
    }

    public ChallengerDataPacket(UUID player, InputStream src) throws IOException {
        super(player, src);
    }

    public ChallengerDataPacket(ByteBuf buffer) {
        super(buffer);
    }

    public ChallengerDataPacket(ByteBuffer buffer) {
        super(buffer);
    }

    @Override
    public byte[] getFormattedData() {
        byte[] data = getData();
        byte[] identifierBytes = getIdentifier().getBytes();
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(data.length + identifierBytes.length);
        byteBuf.writeInt(identifierBytes.length) //Length of the identifier.
                .writeBytes(identifierBytes) //The identifier
                .writeInt(data.length) //The length of the data
                .writeBytes(data); //The actual data
        return byteBuf.array();
    }

    @Override
    public int fromBytes(byte[] bytes) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytes.length);
        buf.writeBytes(bytes);
        int len = buf.readInt();
        buf = buf.readBytes(len);
        byte[] idBytes = buf.slice().array();
        String identifier = new String(idBytes); //Currently unused.
        len = buf.readInt();
        super.setData(buf.readBytes(len).slice().array());
        return buf.readerIndex();
    }

    /**
     * Read from the current packet's data to reconstruct the
     * {@link IChallengerData} object.
     *
     * @return
     */
    public IChallengerData toObject() {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        buf.writeBytes(getData());
        int len = buf.readInt();
        buf = buf.readBytes(len);
        String name = new String(buf.slice().array());
        byte[] icon = buf.slice().array();
        len = buf.readInt();
        buf = buf.readBytes(len);
        byte[] loreData = buf.slice().array();
        List<String> list = new LinkedList<>();
        ByteBuf loreBuf = ByteBufAllocator.DEFAULT.buffer(loreData.length);
        while (loreBuf.readableBytes() > 0) {
            len = loreBuf.readInt();
            loreBuf = loreBuf.readBytes(len);
            byte[] data = loreBuf.slice().array();
            list.add(new String(data));
        }
        return new ChallengerDataImpl(name, icon, new ArrayList<>(list));
    }

    @Override
    public String getIdentifier() {
        return ChallengerDataPacket.class.getCanonicalName() + "::" + VERSION;
    }

}
