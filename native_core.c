#include <windows.h>

// OS 단의 초정밀 하드웨어 타이머 (CPU 틱 카운트)
__declspec(dllexport) long long get_hw_timer() {
    LARGE_INTEGER li;
    QueryPerformanceCounter(&li);
    return li.QuadPart;
}