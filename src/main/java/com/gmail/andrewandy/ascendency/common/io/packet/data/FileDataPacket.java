package com.gmail.andrewandy.ascendency.common.io.packet.data;

import com.gmail.andrewandy.ascendencyserverplugin.io.packet.AscendencyPacket;
import com.gmail.andrewandy.ascendencyserverplugin.io.packet.results.AscendencyResultPacket;
import com.gmail.andrewandy.ascendencyserverplugin.io.packet.results.Result;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.*;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a packet which holds data from a given file on the disk.
 */
public class FileDataPacket extends DataPacket {

    private static final AscendencyResultPacket BAD_DATA_PACKET = new AscendencyResultPacket("BAD_DATA_REC", Result.FAILURE);
    private static final String PROTOCOL_VERSION = "1";
    private static final String SPLITTER = "::";
    private static String dataFolder;
    private String fileName;
    private long targetFileSize;

    public FileDataPacket() {
    }

    public FileDataPacket(File file) throws IOException {
        this(new FileInputStream(file), file.getName(), file.length());
    }


    public FileDataPacket(InputStream src, String fileName, long targetFileSize) throws IOException {
        super(src);
        this.fileName = fileName;
        if (targetFileSize < 0) {
            throw new IllegalArgumentException("Invalid file size!");
        }
        this.targetFileSize = targetFileSize;
    }

    public static void setDataFolder(File dataFolder) {
        FileDataPacket.dataFolder = dataFolder.getAbsolutePath();
    }

    public static AscendencyPacket handleIncomingPacket(FileDataPacket incoming) throws IOException {
        byte[] bytes = incoming.getData();
        String fileName = incoming.fileName;
        if (dataFolder == null) {
            throw new IllegalStateException("No data folder set!");
        }
        File file = new File(dataFolder, fileName);
        file.createNewFile();
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(bytes);
        }
        if (!incoming.fileSizeIsCorrect(file)) {
            file.delete();
            return BAD_DATA_PACKET;
        }
        return AscendencyResultPacket.SUCCESS;
    }

    @Override
    public int fromBytes(byte[] bytes) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytes.length);
        buf.writeBytes(bytes);
        int idLength = Objects.requireNonNull(buf).readInt();
        String identifier = new String(buf.readSlice(idLength).array());
        String[] arr = identifier.split(SPLITTER);
        if (arr.length < 2) {
            throw new IllegalArgumentException("Invalid identifier!");
        }
        try {
            Class<?> clazz = Class.forName(arr[0]);
            if (!FileDataPacket.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Packet identifier not type of FileDataPacket!");
            }
            String otherVersion = arr[1];
            if (!otherVersion.equals(PROTOCOL_VERSION)) {
                //Conversion
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Packet identifier not type of FileDataPacket!");
        }
        int nameLength = buf.readInt();
        assert nameLength > 0;
        byte[] nameBytes = buf.readBytes(nameLength).slice().array();
        fileName = new String(nameBytes);
        targetFileSize = buf.readLong();
        int dataLen = buf.readInt();
        byte[] data = dataLen > 0 ? buf.readBytes(dataLen).slice().array() : new byte[0];
        super.setData(data);
        if (!fileSizeIsCorrect()) {
            throw new IllegalStateException("File size != target file size!");
        }
        return buf.readerIndex();
    }

    private boolean fileSizeIsCorrect(File file) {
        return Objects.requireNonNull(file).length() == targetFileSize;
    }

    private boolean fileSizeIsCorrect() {
        File temp = null;
        try {
            temp = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
            try (FileOutputStream os = new FileOutputStream(temp)) {
                os.write(getData());
                return fileSizeIsCorrect(temp);
            }
        } catch (IOException ex) {
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
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        byte[] nameBytes = fileName.getBytes();
        String identifier = this.getClass().getCanonicalName() + SPLITTER + PROTOCOL_VERSION;
        byte[] identifierBytes = identifier.getBytes();
        byte[] data = getData();
        buf.writeInt(identifierBytes.length)
                .writeBytes(identifierBytes)
                .writeInt(nameBytes.length)
                .writeBytes(nameBytes)
                .writeLong(targetFileSize)
                .writeInt(data.length)
                .writeBytes(data);
        return buf.array();
    }

    public void writeToFile(File file) throws IOException {
        if (file == null) {
            throw new FileNotFoundException("null file!");
        }
        try (OutputStream stream = new FileOutputStream(file)) {
            writeToStream(stream, false);
        }
    }

    @Override
    public String getIdentifier() {
        return FileRequestPacket.class.getCanonicalName() + SPLITTER + PROTOCOL_VERSION;
    }
}
