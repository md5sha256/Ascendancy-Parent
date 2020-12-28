package com.gmail.andrewandy.ascendancy.lib.data;

import com.gmail.andrewandy.ascendancy.lib.AscendancyPacket;
import com.gmail.andrewandy.ascendancy.lib.results.AscendancyResultPacket;
import com.gmail.andrewandy.ascendancy.lib.results.Result;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.*;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a packet which holds data from a given file on the disk.
 */
public class FileDataPacket extends DataPacket {

    private static final AscendancyResultPacket BAD_DATA_PACKET = new AscendancyResultPacket("BAD_DATA_REC", Result.FAILURE);
    private static final String PROTOCOL_VERSION = "1";
    private static final String SPLITTER = "::";
    private static String dataFolder;
    private String fileName;
    private long targetFileSize;

    public FileDataPacket() {
    }

    public FileDataPacket(final UUID player) {
        super(player);
    }

    public FileDataPacket(final UUID player, final File file) throws IOException {
        this(player, new FileInputStream(file), file.getName(), file.length());
    }


    public FileDataPacket(final UUID player, final InputStream src, final String fileName, final long targetFileSize) throws IOException {
        super(player, src);
        this.fileName = fileName;
        if (targetFileSize < 0) {
            throw new IllegalArgumentException("Invalid file size!");
        }
        this.targetFileSize = targetFileSize;
    }

    public static void setDataFolder(final File dataFolder) {
        FileDataPacket.dataFolder = dataFolder.getAbsolutePath();
    }

    public static AscendancyPacket handleIncomingPacket(final FileDataPacket incoming) throws IOException {
        final byte[] bytes = incoming.getData();
        final String fileName = incoming.fileName;
        if (dataFolder == null) {
            throw new IllegalStateException("No data folder set!");
        }
        final File file = new File(dataFolder, fileName);
        file.createNewFile();
        try (final OutputStream os = new FileOutputStream(file)) {
            os.write(bytes);
        }
        if (!incoming.fileSizeIsCorrect(file)) {
            file.delete();
            return BAD_DATA_PACKET;
        }
        return AscendancyResultPacket.SUCCESS;
    }

    @Override
    public int fromBytes(final byte[] bytes) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytes.length);
        buf.writeBytes(bytes);
        final int idLength = Objects.requireNonNull(buf).readInt();
        final String identifier = new String(buf.readSlice(idLength).array());
        buf = buf.readBytes(idLength); //Since buf is immutable we must reassign.
        final String[] arr = identifier.split(SPLITTER);
        if (arr.length < 2) {
            throw new IllegalArgumentException("Invalid identifier!");
        }
        try {
            final Class<?> clazz = Class.forName(arr[0]);
            if (!FileDataPacket.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Packet identifier not type of FileDataPacket!");
            }
            final String otherVersion = arr[1];
            if (!otherVersion.equals(PROTOCOL_VERSION)) {
                //Conversion
            }
        } catch (final ClassNotFoundException ex) {
            throw new IllegalArgumentException("Packet identifier not type of FileDataPacket!");
        }
        final int nameLength = buf.readInt();
        assert nameLength > 0;
        final byte[] nameBytes = buf.readBytes(nameLength).slice().array();
        fileName = new String(nameBytes);
        targetFileSize = buf.readLong();
        final int dataLen = buf.readInt();
        final byte[] data = dataLen > 0 ? buf.readBytes(dataLen).slice().array() : new byte[0];
        super.setData(data);
        if (!fileSizeIsCorrect()) {
            throw new IllegalStateException("File size != target file size!");
        }
        return buf.readerIndex();
    }

    private boolean fileSizeIsCorrect(final File file) {
        return Objects.requireNonNull(file).length() == targetFileSize;
    }

    private boolean fileSizeIsCorrect() {
        File temp = null;
        try {
            temp = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
            try (final FileOutputStream os = new FileOutputStream(temp)) {
                os.write(getData());
                return fileSizeIsCorrect(temp);
            }
        } catch (final IOException ex) {
            return false;
        } finally {
            if (temp != null) {
                temp.delete();
            }
        }
    }


    @Override
    public byte[] getFormattedData() {
        if (fileName == null) {
            return new byte[0];
        }
        final ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        final byte[] nameBytes = fileName.getBytes();
        final String identifier = getIdentifier();
        final byte[] identifierBytes = identifier.getBytes();
        final byte[] data = getData();
        buf.writeInt(identifierBytes.length)
                .writeBytes(identifierBytes)
                .writeInt(nameBytes.length)
                .writeBytes(nameBytes)
                .writeLong(targetFileSize)
                .writeInt(data.length)
                .writeBytes(data);
        return buf.array();
    }

    public void writeToFile(final File file) throws IOException {
        if (file == null) {
            throw new FileNotFoundException("null file!");
        }
        try (final OutputStream stream = new FileOutputStream(file)) {
            writeToStream(stream, false);
        }
    }

    @Override
    public String getIdentifier() {
        return FileRequestPacket.class.getCanonicalName() + SPLITTER + PROTOCOL_VERSION;
    }
}
