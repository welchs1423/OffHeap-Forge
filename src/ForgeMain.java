import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

void main() throws Exception {
    long dataSize = 1024 * 1024 * 16;
    Path snapshotPath = Path.of("snapshot.bin");

    try (Arena arena = Arena.ofConfined()) {
        MemorySegment memoryDb = arena.allocate(dataSize);
        memoryDb.set(ValueLayout.JAVA_LONG, 0, 20260319L);
        memoryDb.set(ValueLayout.JAVA_DOUBLE, 8, 99.99);

        System.out.println("In Memory DB Running");
        System.out.println("Original Data ID: " + memoryDb.get(ValueLayout.JAVA_LONG, 0));

        try (FileChannel channel = FileChannel.open(snapshotPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE)) {

            MemorySegment mmapFile = channel.map(FileChannel.MapMode.READ_WRITE, 0, dataSize, arena);
            MemorySegment.copy(memoryDb, 0, mmapFile, 0, dataSize);

            System.out.println("Snapshot Dump Complete snapshot.bin");
        }

        memoryDb.fill((byte) 0);
        System.out.println("System Crashed Data in RAM: " + memoryDb.get(ValueLayout.JAVA_LONG, 0));

        try (FileChannel channel = FileChannel.open(snapshotPath, StandardOpenOption.READ)) {
            MemorySegment mmapFile = channel.map(FileChannel.MapMode.READ_ONLY, 0, dataSize, arena);
            MemorySegment.copy(mmapFile, 0, memoryDb, 0, dataSize);

            System.out.println("System Recovered from Snapshot");
            System.out.println("Restored Data ID: " + memoryDb.get(ValueLayout.JAVA_LONG, 0));
            System.out.println("Restored Data Value: " + memoryDb.get(ValueLayout.JAVA_DOUBLE, 8));
        }
    }
}