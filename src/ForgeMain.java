import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

void main() {
    // int(4바이트) + 패딩(4바이트) + double(8바이트) = 총 16바이트
    var userLayout = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("id"),
            MemoryLayout.paddingLayout(4),
            ValueLayout.JAVA_DOUBLE.withName("score")
    );

    try (Arena arena = Arena.ofConfined()) {
        MemorySegment segment = arena.allocate(userLayout);

        // id는 0번 인덱스
        segment.set(ValueLayout.JAVA_INT, 0, 1001);

        // score는 int(4) + 패딩(4) 이후인 8번 인덱스
        segment.set(ValueLayout.JAVA_DOUBLE, 8, 98.5);

        System.out.println("---------------------------------");
        System.out.println("Total size: " + userLayout.byteSize() + " bytes");
        System.out.println("ID: " + segment.get(ValueLayout.JAVA_INT, 0));
        System.out.println("Score: " + segment.get(ValueLayout.JAVA_DOUBLE, 8));
        System.out.println("---------------------------------");
        System.out.println("OffHeap-Forge: Success with Padding");
    }
}