import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles;

class Sequence {
    // CPU 캐시 라인(64바이트)을 독점하기 위한 앞단 패딩
    protected long p1, p2, p3, p4, p5, p6, p7;

    // 실제 핵심 데이터 (Index)
    private volatile long value = 0;

    // 뒷단 패딩
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

public class ForgeMain {
    public static void main(String[] args) {
        Sequence seq = new Sequence();

        System.out.println("Mechanical Sympathy: Cache Line Padding Activated");
        long start = System.nanoTime();

        for (long i = 0; i < 100000000; i++) {
            seq.set(i);
        }

        long end = System.nanoTime();
        System.out.println("100 Million Atomic Updates Complete");
        System.out.println("Execution Time: " + (end - start) / 1000000 + " ms");
    }
}