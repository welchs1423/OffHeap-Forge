import mmap
import struct
import time
import os

print("🐍 [Python Consumer] 1:N Polyglot Shared Memory Online!")

filepath = "forge-data.dat"

# 파일이 생길 때까지 대기
while not os.path.exists(filepath) or os.path.getsize(filepath) == 0:
    time.sleep(1)

# 자바가 뚫어놓은 공유 메모리에 빨대 꽂기 (읽기 전용)
with open(filepath, "r+b") as f:
    mm = mmap.mmap(f.fileno(), 0, access=mmap.ACCESS_READ)

    cursor_offset = 1024 * 8
    last_read_idx = 0

    while True:
        mm.seek(cursor_offset)
        # 8바이트 Little Endian 읽기
        current_tail = struct.unpack('<Q', mm.read(8))[0]

        if current_tail > last_read_idx:
            target_idx = (current_tail - 1) % 1024
            mm.seek(target_idx * 8)
            val = struct.unpack('<Q', mm.read(8))[0]

            print(f"🐍 [Python 훔쳐보기👀] 얍! 나도 읽음! 데이터: {val}")
            last_read_idx = current_tail

        time.sleep(0.01)