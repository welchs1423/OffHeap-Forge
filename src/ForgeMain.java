import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.lang.invoke.VarHandle;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;

void main() throws Exception {
    Path filePath = Path.of("ipc_queue.bin");
    try (FileChannel channel = FileChannel.open(filePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE);
         Arena arena = Arena.ofShared()) {

        long queueCapacity = 10000;
        long elementSize = 8;
        long headerSize = 8;
        long fileSize = headerSize + (queueCapacity * elementSize);

        MemorySegment mmap = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize, arena);
        VarHandle indexHandle = ValueLayout.JAVA_LONG.varHandle();

        mmap.set(ValueLayout.JAVA_LONG, 0, 0L);

        int threadCount = 10000;
        CountDownLatch latch = new CountDownLatch(threadCount);

        long startTime = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    long currentIndex = (long) indexHandle.getAndAdd(mmap, 0L, 1L);
                    long safeIndex = currentIndex % queueCapacity;
                    long dataOffset = headerSize + (safeIndex * elementSize);

                    mmap.set(ValueLayout.JAVA_LONG, dataOffset, (long) threadId);
                    latch.countDown();
                });
            }
        }

        latch.await();
        long endTime = System.currentTimeMillis();

        System.out.println("Stress Test Complete");
        System.out.println("Target Threads: " + threadCount);
        System.out.println("Final Index Header: " + mmap.get(ValueLayout.JAVA_LONG, 0));
        System.out.println("Elapsed Time: " + (endTime - startTime) + " ms");
    }
}