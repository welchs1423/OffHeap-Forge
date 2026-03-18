import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class ForgeMain {
    public static void main(String[] args) {
        long capacity = 2000000;
        long slotSize = 16;
        long hashMemorySize = capacity * slotSize;

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment hashTable = arena.allocate(hashMemorySize);

            System.out.println("Off-Heap Hash Index Engine Start");
            long startTime = System.nanoTime();

            long targetKey = 999999L;
            long targetValue = 7777777L;

            long hash = (targetKey ^ (targetKey >>> 16)) % capacity;
            long offset = hash * slotSize;

            hashTable.set(ValueLayout.JAVA_LONG, offset, targetKey);
            hashTable.set(ValueLayout.JAVA_LONG, offset + 8, targetValue);

            long searchKey = 999999L;
            long searchHash = (searchKey ^ (searchKey >>> 16)) % capacity;
            long searchOffset = searchHash * slotSize;

            long foundKey = hashTable.get(ValueLayout.JAVA_LONG, searchOffset);
            long foundValue = hashTable.get(ValueLayout.JAVA_LONG, searchOffset + 8);

            long endTime = System.nanoTime();

            if (foundKey == searchKey) {
                System.out.println("Data Found!");
                System.out.println("Search Key: " + foundKey);
                System.out.println("Found Value: " + foundValue);
                System.out.println("Execution Time: " + (endTime - startTime) + " ns");
            } else {
                System.out.println("Data Not Found");
            }
        }
    }
}