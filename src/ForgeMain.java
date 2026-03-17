import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

void main() {
    var userLayout = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("id"),
            MemoryLayout.paddingLayout(4),
            ValueLayout.JAVA_DOUBLE.withName("score")
    );

    // 1. 1,000,000개 (백만 개) 데이터 공간 설계
    int elementCount = 1_000_000;
    var arrayLayout = MemoryLayout.sequenceLayout(elementCount, userLayout);

    VarHandle idHandle = arrayLayout.varHandle(
            MemoryLayout.PathElement.sequenceElement(),
            MemoryLayout.PathElement.groupElement("id")
    );
    VarHandle scoreHandle = arrayLayout.varHandle(
            MemoryLayout.PathElement.sequenceElement(),
            MemoryLayout.PathElement.groupElement("score")
    );

    try (Arena arena = Arena.ofConfined()) {
        long startTime = System.currentTimeMillis();

        // 2. 백만 개 분량의 메모리 할당 (약 16MB)
        MemorySegment segment = arena.allocate(arrayLayout);

        // 3. 루프를 돌며 백만 개 데이터 채우기
        for (long i = 0; i < elementCount; i++) {
            idHandle.set(segment, 0L, i, (int) i);
            scoreHandle.set(segment, 0L, i, i * 1.5);
        }

        long endTime = System.currentTimeMillis();

        System.out.println("---------------------------------");
        System.out.println("데이터 개수: " + elementCount + " 개");
        System.out.println("메모리 할당 크기: " + arrayLayout.byteSize() / 1024 / 1024 + " MB");
        System.out.println("소요 시간: " + (endTime - startTime) + " ms");
        System.out.println("마지막 데이터 ID: " + idHandle.get(segment, 0L, (long) elementCount - 1));
        System.out.println("---------------------------------");
        System.out.println("OffHeap-Forge: Massive Scale Test Success");
    }
}