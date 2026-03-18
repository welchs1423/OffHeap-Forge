import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ForgeMain {
    public static void main(String[] args) throws Exception {
        long capacity = 100000;
        long slotSize = 16;
        long fileSize = capacity * slotSize;

        Path dbPath = Path.of("local_nosql.bin");

        try (FileChannel channel = FileChannel.open(dbPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE);
             Arena arena = Arena.ofShared()) {

            MemorySegment mmapDb = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize, arena);

            System.out.println("Persistent NoSQL Engine Start");

            long insertKey = 8282;
            long insertValue = 10041004;

            long hash = (insertKey ^ (insertKey >>> 16)) % capacity;
            long offset = hash * slotSize;

            mmapDb.set(ValueLayout.JAVA_LONG, offset, insertKey);
            mmapDb.set(ValueLayout.JAVA_LONG, offset + 8, insertValue);

            System.out.println("Data Written to Disk via MMAP (Zero-Copy)");

            long readHash = (insertKey ^ (insertKey >>> 16)) % capacity;
            long readOffset = readHash * slotSize;

            long foundKey = mmapDb.get(ValueLayout.JAVA_LONG, readOffset);
            long foundValue = mmapDb.get(ValueLayout.JAVA_LONG, readOffset + 8);

            if (foundKey == insertKey) {
                System.out.println("Data Retrieved Directly from OS Cache");
                System.out.println("Key: " + foundKey);
                System.out.println("Value: " + foundValue);
                System.out.println("Check local_nosql.bin in your project folder!");
            }
        }
    }
}