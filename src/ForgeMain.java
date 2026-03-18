import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles;

// 이 클래스는 public이 아니므로 ForgeMain.java 안에 함께 있어도 됩니다.
class Sequence {
    // 64바이트 캐시 라인을 채우기 위한 앞단 패딩 (8바이트 * 7 = 56바이트)
    protected long p1, p2, p3, p4, p5, p6, p7;

    // 실제 데이터 (8바이트) -> 앞단 패딩과 합쳐져 딱 64바이트가 됩니다.
    private volatile long value = 0;

    // 뒷단 패딩 (8바이트 * 7 = 56바이트) -> 다른 객체와의 간격 확보
    protected long p8, p9, p10, p11, p12, p13, p14;

    private static final VarHandle VALUE_HANDLE;

    static {
        try {
            VALUE_HANDLE = MethodHandles.lookup()
                    .findVarHandle(Sequence.class, "value", long.class);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    public long get() {
        return value;
    }

    public void set(long newValue) {
        VALUE_HANDLE.setVolatile(this, newValue);
    }
}

// 파일 이름은 반드시 ForgeMain.java 여야 합니다.
public class ForgeMain {
    public static void main(String[] args) {
        Sequence seq = new Sequence();

        System.out.println("Mechanical Sympathy: Cache Line Padding Activated");
        long start = System.nanoTime();

        // 1억 번의 원자적 업데이트 테스트
        for (long i = 0; i < 100000000; i++) {
            seq.set(i);
        }

        long end = System.nanoTime();
        System.out.println("100 Million Atomic Updates Complete");
        System.out.println("Execution Time: " + (end - start) / 1000000 + " ms");
    }
}