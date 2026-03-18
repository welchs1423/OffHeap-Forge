import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;

void main() throws Throwable {
    System.load(Path.of("engine.dll").toAbsolutePath().toString());

    Linker linker = Linker.nativeLinker();
    SymbolLookup lookup = SymbolLookup.loaderLookup();

    MethodHandle calculateRisk = linker.downcallHandle(
            lookup.find("calculateRisk").get(),
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
    );

    try (Arena arena = Arena.ofConfined()) {
        int size = 10000;
        MemorySegment nativeArray = arena.allocate(ValueLayout.JAVA_LONG, size);

        for (int i = 0; i < size; i++) {
            nativeArray.setAtIndex(ValueLayout.JAVA_LONG, i, i + 1);
        }

        long startTime = System.nanoTime();
        long result = (long) calculateRisk.invokeExact(nativeArray, size);
        long endTime = System.nanoTime();

        System.out.println("FFM Native Downcall Success");
        System.out.println("Calculated Risk Sum: " + result);
        System.out.println("Execution Time: " + (endTime - startTime) + " ns");
    }
}