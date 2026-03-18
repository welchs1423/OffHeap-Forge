import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Path;

public class ForgeMain {

    public static void onProgress(int current) {
        System.out.println("C Engine Callback Received Processed Count: " + current);
    }

    public static void main(String[] args) throws Throwable {
        System.load(Path.of("callback_engine.dll").toAbsolutePath().toString());

        Linker linker = Linker.nativeLinker();
        SymbolLookup lookup = SymbolLookup.loaderLookup();

        MethodHandle processWithCallback = linker.downcallHandle(
                lookup.find("processWithCallback").get(),
                FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
        );

        MethodHandle javaCallbackHandle = MethodHandles.lookup().findStatic(
                ForgeMain.class, "onProgress", MethodType.methodType(void.class, int.class)
        );

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment callbackStub = linker.upcallStub(
                    javaCallbackHandle,
                    FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT),
                    arena
            );

            System.out.println("FFM Native Upcall Start");
            processWithCallback.invokeExact(3000, callbackStub);
            System.out.println("C Engine Processing Complete");
        }
    }
}