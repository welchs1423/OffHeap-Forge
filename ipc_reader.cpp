#include <windows.h>
#include <iostream>

int main() {
    HANDLE hFile = CreateFileA("ipc_queue.bin", GENERIC_READ, FILE_SHARE_READ | FILE_SHARE_WRITE, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
    if (hFile == INVALID_HANDLE_VALUE) {
        std::cout << "File not found. Run Java producer first.\n";
        return 1;
    }

    HANDLE hMap = CreateFileMappingA(hFile, NULL, PAGE_READONLY, 0, 0, NULL);
    void* pBuf = MapViewOfFile(hMap, FILE_MAP_READ, 0, 0, 0);

    long long* dataPtr = static_cast<long long*>(pBuf);
    long long currentIndex = dataPtr[0];

    std::cout << "C++ HFT Consumer Start\n";
    std::cout << "Java Queue Current Index: " << currentIndex << "\n";

    if (currentIndex > 0) {
        long long targetIndex = (currentIndex - 1) % 10000;
        long long lastData = dataPtr[1 + targetIndex];
        std::cout << "Intercepted Data from Java: " << lastData << "\n";
    }

    UnmapViewOfFile(pBuf);
    CloseHandle(hMap);
    CloseHandle(hFile);

    return 0;
}