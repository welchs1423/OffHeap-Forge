__declspec(dllexport) long long calculateRisk(long long* data, int size) {
    long long sum = 0;
    for (int i = 0; i < size; i++) {
        sum += data[i];
    }
    return sum;
}