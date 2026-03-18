__declspec(dllexport) void processWithCallback(int limit, void (*callback)(int)) {
    for (int i = 1; i <= limit; i++) {
        if (i % 1000 == 0) {
            callback(i);
        }
    }
}