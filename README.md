# 🚀 OffHeap-Forge: Ultra-Low Latency Data Pipeline (V1.0)

## 📌 프로젝트 개요 (Overview)
**OffHeap-Forge**는 이기종 언어(Rust, Java, C++)와 Oracle DB를 결합하여 만든 **초저지연 실시간 데이터 처리 파이프라인 및 모니터링 대시보드**입니다.
GC(Garbage Collection) 딜레이를 극한으로 회피하기 위해 Java의 **Off-Heap 메모리(FFM API)**와 **SIMD(Vector API)**를 활용하였으며, 초당 수백 건의 트랜잭션(TPS)을 병목 없이 Oracle DB에 적재하고 웹으로 실시간 스트리밍합니다.

## ⚙️ 코어 아키텍처 (Core Architecture)
1. **Rust Producer (Data Factory):** TCP 소켓을 통해 마이크로초 단위로 메트릭 데이터를 생성 및 전송.
2. **Java Consumer (Zero-Contention Engine):** - Non-blocking NIO로 데이터를 수신하여 1024-버퍼 크기의 **Off-Heap Memory(MemorySegment)**에 직접 기록 (JVM GC 개입 차단).
    - **C/C++ Native Hardware Timer**를 JNI/FFM API로 바인딩하여 마이크로초(μs) 단위 프로파일링 수행.
3. **SIMD Vectorized Flusher:** Java Incubator Vector API를 활용해 Off-Heap 데이터를 4건씩 묶어(Batch) Oracle DB에 초고속 `MERGE INTO` 수행.
4. **Smart Recovery Engine:** 서버 재가동 시 Oracle DB의 `MAX(SEQ_ID)`를 조회하여 딜레이(예열) 없이 파이프라인 즉시 복구.

## 📊 사이버펑크 실시간 대시보드 (Real-Time Dashboard)
- **100ms Polling & Cache Busting:** Spring MVC(JSP) 기반으로 0.1초마다 DB를 조회하여 화면 새로고침 없이 Chart.js 심전도 그래프 렌더링.
- **Live TPS Meter & Alert Terminal:** 현재 초당 처리량(TPS) 실시간 계산 및 임계치(Overload Spike) 돌파 시 하단 터미널에 `[CRITICAL]` 로그 누적.
- **Kill-Switch & 명예의 전당:** 실시간 차트를 동결시키는 PAUSE 스위치 및 세션 내 가장 높은 수치를 기록한 `TOP 5 SPIKES` 랭킹 보드 탑재.

## 🛠️ 기술 스택 (Tech Stack)
- **Languages:** Java 25 (FFM API, Vector API), Rust, C/C++, JavaScript, HTML/CSS
- **Database:** Oracle Database (Docker, XEPDB1)
- **Web Framework:** Spring Web MVC, Apache Tomcat 10
- **Libraries:** Chart.js, JNA/JNI