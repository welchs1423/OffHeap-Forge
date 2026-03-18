import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class ForgeMain {
    public static void main(String[] args) {
        long poolSize = 1024 * 1024 * 1024;
        long objectSize = 64;

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment memoryPool = arena.allocate(poolSize);
            long currentOffset = 0;

            System.out.println("Zero-GC Slab Allocator Start 1GB Pool");
            long startTime = System.nanoTime();

            int allocationCount = 1000000;
            for (int i = 0; i < allocationCount; i++) {
                long assignedOffset = currentOffset;
                currentOffset += objectSize;

                if (currentOffset <= poolSize) {
                    MemorySegment objectSpace = memoryPool.asSlice(assignedOffset, objectSize);
                    objectSpace.set(ValueLayout.JAVA_LONG, 0, (long) i);
                } else {
                    System.out.println("Out of Memory in Custom Pool");
                    break;
                }
            }

            long endTime = System.nanoTime();
            System.out.println("Allocated 1000000 Objects in Off-Heap without OS Calls");
            System.out.println("Execution Time: " + (endTime - startTime) / 1000000 + " ms");
            System.out.println("Used Memory: " + currentOffset + " bytes");
        }
    }
}