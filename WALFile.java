
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

class WALFile implements WALInterface {

    private static final Path WAL_PATH = Path.of("wal.bin");

    @Override
    public boolean writeIntoWAL(String operation) {
        byte[] data = operation.getBytes(StandardCharsets.UTF_8);

        CRC32 crc = new CRC32();
        crc.update(data);
        int checksum = (int) crc.getValue();

        ByteBuffer header = ByteBuffer.allocate(8);
        header.putInt(data.length);
        header.putInt(checksum);
        header.flip();

        try (FileChannel ch = FileChannel.open(WAL_PATH,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            ch.write(header);
            ch.write(ByteBuffer.wrap(data));
            ch.force(true);
            return true;
        } catch (IOException e) {
            System.err.println("WAL write failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> replay() throws IOException {
        List<String> operations = new ArrayList<>();

        if (!Files.exists(WAL_PATH)) return operations;

        try (FileChannel ch = FileChannel.open(WAL_PATH, StandardOpenOption.READ)) {
            while (true) {
                ByteBuffer header = ByteBuffer.allocate(8);
                if (ch.read(header) <= 0) break;

                header.flip();
                int length = header.getInt();
                int storedChecksum = header.getInt();

                ByteBuffer body = ByteBuffer.allocate(length);
                ch.read(body);
                body.flip();

                byte[] data = new byte[length];
                body.get(data);

                CRC32 crc = new CRC32();
                crc.update(data);
                if ((int) crc.getValue() != storedChecksum) {
                    System.err.println("Corrupt record at index " + operations.size());
                    break;
                }

                operations.add(new String(data, StandardCharsets.UTF_8));
            }
        }

        return operations;
    }
}
