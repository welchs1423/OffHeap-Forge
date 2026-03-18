import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

void main() throws Exception {
    Path filePath = Path.of("offheap_data.bin");

    try (FileChannel channel = FileChannel.open(filePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE);
         Arena arena = Arena.ofConfined()) {

        long fileSize = 1024;

        MemorySegment mmapSegment = channel.map(
                FileChannel.MapMode.READ_WRITE,
                0,
                fileSize,
                arena
        );

        mmapSegment.set(ValueLayout.JAVA_LONG, 0, 20260318L);
        mmapSegment.set(ValueLayout.JAVA_DOUBLE, 8, 3.14159);

        long readLong = mmapSegment.get(ValueLayout.JAVA_LONG, 0);
        double readDouble = mmapSegment.get(ValueLayout.JAVA_DOUBLE, 8);

        System.out.println("MMAP System Start");
        System.out.println("Read Long: " + readLong);
        System.out.println("Read Double: " + readDouble);
        System.out.println("Check the offheap_data.bin file in your project folder.");
    }
}