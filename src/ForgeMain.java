import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

void main() {
    // 1. 단일 사용자 구조체 레이아웃 (int + padding + double)
    var userLayout = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("id"),
            MemoryLayout.paddingLayout(4),
            ValueLayout.JAVA_DOUBLE.withName("score")
    );

    // 2. 구조체 10개를 이어 붙인 '시퀀스(배열)' 레이아웃 생성
    var arrayLayout = MemoryLayout.sequenceLayout(10, userLayout);

    // 3. 특정 인덱스에 접근하기 위한 VarHandle (포인터 연산의 현대적 방식)
    // "5번째 유저의 id" 같은 위치를 자바가 계산해 주도록 설정합니다.
    VarHandle idHandle = arrayLayout.varHandle(
            MemoryLayout.PathElement.sequenceElement(),
            MemoryLayout.PathElement.groupElement("id")
    );

    try (Arena arena = Arena.ofConfined()) {
        // 10명분 메모리 한 번에 할당 (160바이트)
        MemorySegment segment = arena.allocate(arrayLayout);

        // 4. 5번째 사용자(인덱스 4)의 ID를 777로 설정
        // 직접 오프셋을 계산하지 않아도 idHandle이 알아서 위치를 찾아갑니다.
        idHandle.set(segment, 0L, 4L, 777);

        System.out.println("---------------------------------");
        System.out.println("배열 전체 크기: " + arrayLayout.byteSize() + " bytes");
        System.out.println("5번째 사용자 ID: " + idHandle.get(segment, 0L, 4L));
        System.out.println("---------------------------------");
        System.out.println("OffHeap-Forge: SequenceLayout Success");
    }
}