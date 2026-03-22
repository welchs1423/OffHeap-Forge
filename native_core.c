#include <windows.h>

// 1. CPU 현재 틱 카운트 가져오기
__declspec(dllexport) long long get_hw_timer() {
    LARGE_INTEGER li;
    QueryPerformanceCounter(&li);
    return li.QuadPart;
}

// 2. CPU 주파수(1초당 틱 수) 가져오기 -> 이걸 알아야 마이크로초 단위 계산이 가능합니다!
__declspec(dllexport) long long get_hw_freq() {
    LARGE_INTEGER li;
    QueryPerformanceFrequency(&li);
    return li.QuadPart;
}