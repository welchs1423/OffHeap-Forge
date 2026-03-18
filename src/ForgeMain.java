import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.lang.invoke.VarHandle;

void main() throws Exception {
    Path filePath = Path.of("ipc_queue.bin");
    try (FileChannel channel = FileChannel.open(filePath,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE);
         Arena arena = Arena.ofShared()) {

        long queueCapacity = 10000;
        long elementSize = 8;
        long headerSize = 8;
        long fileSize = headerSize + (queueCapacity * elementSize);

        MemorySegment mmap = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize, arena);
        VarHandle indexHandle = ValueLayout.JAVA_LONG.varHandle();

        long readIndex = 0;
        long emptySpinCount = 0;
        long maxSpins = 1000;

        System.out.println("Consumer Start");

        while (emptySpinCount < maxSpins) {
            long writeIndex = (long) indexHandle.getVolatile(mmap, 0L);

            if (readIndex < writeIndex) {
                long safeIndex = readIndex % queueCapacity;
                long dataOffset = headerSize + (safeIndex * elementSize);

                long data = mmap.get(ValueLayout.JAVA_LONG, dataOffset);

                if (readIndex % 2000 == 0 || readIndex == writeIndex - 1) {
                    System.out.println("Read Index: " + readIndex + " / Data: " + data);
                }

                readIndex++;
                emptySpinCount = 0;
            } else {
                emptySpinCount++;
                Thread.sleep(1);
            }
        }

        System.out.println("Consumer End");
        System.out.println("Total Processed: " + readIndex);
    }
}