import mmap
import struct
import os

def main():
    file_path = "ipc_queue.bin"

    if not os.path.exists(file_path):
        print("Error: ipc_queue.bin not found. Run Java producer first.")
        return

    with open(file_path, "r+b") as f:
        mm = mmap.mmap(f.fileno(), 0)

        header_bytes = mm[0:8]
        current_index = struct.unpack("<q", header_bytes)[0]

        print("Polyglot IPC Consumer Start (Python)")
        print("Java Queue Current Index:", current_index)

        if current_index > 0:
            target_index = (current_index - 1) % 10000
            offset = 8 + (target_index * 8)

            data_bytes = mm[offset:offset+8]
            last_data = struct.unpack("<q", data_bytes)[0]

            print("Intercepted Data from Java:", last_data)

        mm.close()

if __name__ == "__main__":
    main()