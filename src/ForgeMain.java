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
            StandardOpenOption.CREATE,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE);
         Arena arena = Arena.ofConfined()) {

        long queueCapacity = 1024;
        long elementSize = 8;
        long headerSize = 8;
        long fileSize = headerSize + (queueCapacity * elementSize);

        MemorySegment mmap = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize, arena);
        VarHandle indexHandle = ValueLayout.JAVA_LONG.varHandle();

        long currentIndex = (long) indexHandle.getAndAdd(mmap, 0L, 1L);
        long safeIndex = currentIndex % queueCapacity;

        long dataOffset = headerSize + (safeIndex * elementSize);

        long businessData = 9999L + currentIndex;
        mmap.set(ValueLayout.JAVA_LONG, dataOffset, businessData);

        System.out.println("Ring Buffer Write Success");
        System.out.println("Absolute Index: " + currentIndex);
        System.out.println("Circular Index: " + safeIndex);
        System.out.println("Written Data: " + businessData);
    }
}