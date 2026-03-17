import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

void main() {
    // 1. 8바이트(long) 크기의 카운터 레이아웃
    var layout = ValueLayout.JAVA_LONG;

    // 2. 원자적 연산을 위한 VarHandle 생성
    VarHandle atomicHandle = layout.varHandle();

    try (Arena arena = Arena.ofConfined()) {
        MemorySegment counter = arena.allocate(layout);

        // 초기값 0 설정
        atomicHandle.set(counter, 0L, 0L);

        // 3. getAndAdd: 현재 값을 가져오고 100을 더함 (원자적 연산)
        // 여러 스레드가 동시에 붙어도 데이터가 꼬이지 않는 마법입니다.
        long oldValue = (long) atomicHandle.getAndAdd(counter, 0L, 100L);
        long newValue = (long) atomicHandle.get(counter, 0L);

        System.out.println("---------------------------------");
        System.out.println("이전 값(Old Value): " + oldValue);
        System.out.println("현재 값(New Value): " + newValue);

        // 4. Compare-And-Swap (CAS): 값이 100이면 500으로 바꿔라!
        boolean success = atomicHandle.compareAndSet(counter, 0L, 100L, 500L);

        System.out.println("CAS 성공 여부: " + success);
        System.out.println("최종 값: " + atomicHandle.get(counter, 0L));
        System.out.println("---------------------------------");
        System.out.println("OffHeap-Forge: Atomic Operations Mastered");
    }
}