import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

void main() {
    // 1. Arena를 통해 메모리 관리 (try-with-resources로 자동 해제)
    try (Arena arena = Arena.ofConfined()) {

        // 2. 8바이트 메모리 할당 (C의 malloc과 유사)
        MemorySegment segment = arena.allocate(8L);

        // 3. 메모리에 직접 long 값 쓰기
        segment.set(ValueLayout.JAVA_LONG, 0, 20260318L);

        // 4. 메모리에서 값 읽어오기
        long value = segment.get(ValueLayout.JAVA_LONG, 0);

        System.out.println("---------------------------------");
        System.out.println("Memory Address: " + segment.address());
        System.out.println("Read Value: " + value);
        System.out.println("---------------------------------");
        System.out.println("Modern Off-Heap Forge Start");
    }
}