import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

void main() throws Exception {
    long cacheLineSize = 64;

    MemoryLayout paddedLayout = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("value"),
            MemoryLayout.paddingLayout(56)
    );

    int threadCount = 4;
    MemoryLayout arrayLayout = MemoryLayout.sequenceLayout(threadCount, paddedLayout);

    VarHandle valueHandle = arrayLayout.varHandle(
            MemoryLayout.PathElement.sequenceElement(),
            MemoryLayout.PathElement.groupElement("value")
    );

    try (Arena arena = Arena.ofShared()) {
        MemorySegment segment = arena.allocate(arrayLayout);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long startTime = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                executor.submit(() -> {
                    long iterations = 10000000L;
                    for (long j = 0; j < iterations; j++) {
                        long current = (long) valueHandle.get(segment, 0L, (long) threadIndex);
                        valueHandle.set(segment, 0L, (long) threadIndex, current + 1L);
                    }
                    latch.countDown();
                });
            }
        }

        latch.await();
        long endTime = System.currentTimeMillis();

        System.out.println("False Sharing Prevented Architecture");
        System.out.println("Total Time: " + (endTime - startTime) + " ms");

        for (int i = 0; i < threadCount; i++) {
            System.out.println("Thread " + i + " Value: " + valueHandle.get(segment, 0L, (long) i));
        }
    }
}