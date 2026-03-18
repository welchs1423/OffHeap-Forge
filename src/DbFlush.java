import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.lang.invoke.VarHandle;

void main() throws Exception {
    Path filePath = Path.of("ipc_queue.bin");
    try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ);
         Arena arena = Arena.ofShared()) {

        long queueCapacity = 10000;
        long elementSize = 8;
        long headerSize = 8;
        long fileSize = headerSize + (queueCapacity * elementSize);

        MemorySegment mmap = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize, arena);
        VarHandle indexHandle = ValueLayout.JAVA_LONG.varHandle();

        long dbSavedIndex = 0;
        int batchSize = 1000;
        long[] batchData = new long[batchSize];

        System.out.println("Write-Behind DB Flusher Start");
        System.out.println("Monitoring OffHeap Queue for Oracle DB Bulk Insert...");

        while (true) {
            long currentWriteIndex = (long) indexHandle.getVolatile(mmap, 0L);
            long pendingCount = currentWriteIndex - dbSavedIndex;

            if (pendingCount >= batchSize) {
                for (int i = 0; i < batchSize; i++) {
                    long safeIndex = (dbSavedIndex + i) % queueCapacity;
                    long dataOffset = headerSize + (safeIndex * elementSize);
                    batchData[i] = mmap.get(ValueLayout.JAVA_LONG, dataOffset);
                }

                System.out.println("--------------------------------------------------");
                System.out.println("Traffic Spike Detected! Executing Batch Insert to DB");
                System.out.println("INSERT INTO orders (id) VALUES (" + batchData[0] + ") ... to (" + batchData[batchSize - 1] + ")");

                dbSavedIndex += batchSize;
                System.out.println("DB Saved Index Updated: " + dbSavedIndex);
                System.out.println("Remaining Pending Data: " + (currentWriteIndex - dbSavedIndex));
                System.out.println("--------------------------------------------------");
                break;
            } else {
                Thread.sleep(100);
            }
        }
    }
}