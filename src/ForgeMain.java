import java.lang.foreign.*;

void main() {
    // 1. 레이아웃 설정
    var userLayout = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("id"),
            MemoryLayout.paddingLayout(4),
            ValueLayout.JAVA_DOUBLE.withName("score")
    );
    int elementCount = 1_000_000;
    var arrayLayout = MemoryLayout.sequenceLayout(elementCount, userLayout);

    try (Arena arena = Arena.ofConfined()) {
        MemorySegment bigSegment = arena.allocate(arrayLayout);

        // 2. 500,000번째 위치에 데이터 하나만 써보기 (직접 계산 방식)
        long offset = 500_000 * userLayout.byteSize();
        bigSegment.set(ValueLayout.JAVA_INT, offset, 500);
        bigSegment.set(ValueLayout.JAVA_DOUBLE, offset + 8, 99.9);

        // 3. 🔥 Zero-copy Slice: 500,000번째 데이터만 바라보는 '가상 세그먼트' 생성
        // 메모리 복사 0! 오직 주소값만 가리키는 16바이트짜리 '창문'입니다.
        MemorySegment slice = bigSegment.asSlice(offset, userLayout.byteSize());

        System.out.println("---------------------------------");
        System.out.println("전체 메모리 크기: " + bigSegment.byteSize() + " bytes");
        System.out.println("슬라이스 크기: " + slice.byteSize() + " bytes (딱 1인분)");

        // 4. 슬라이스를 통해 데이터 읽기 (슬라이스 기준으로는 0번지가 500,000번째 데이터)
        System.out.println("슬라이스로 읽은 ID: " + slice.get(ValueLayout.JAVA_INT, 0));
        System.out.println("슬라이스로 읽은 Score: " + slice.get(ValueLayout.JAVA_DOUBLE, 8));
        System.out.println("---------------------------------");
        System.out.println("OffHeap-Forge: Zero-copy Slicing Success");
    }
}