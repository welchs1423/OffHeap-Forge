# OffHeap-Forge

JDK 25의 **Foreign Function & Memory (FFM) API**를 활용하여 자바의 성능 한계에 도전하는 프로젝트입니다.

## 🎯 프로젝트 목표
- **Zero-GC**: JVM 힙 외부 메모리를 직접 관리하여 가비지 컬렉션으로 인한 멈춤(Stop-the-world)을 제거합니다.
- **Ultra-Low Latency**: 마이크로초 단위 이하의 초저지연 데이터 처리를 지향합니다.
- **Modern Java**: JDK 21+의 최신 표준 API(Arena, MemorySegment)를 적극 활용합니다.

## 🛠 기술 스택
- **Language**: Java 25
- **Core**: Foreign Function & Memory API (Project Panama)
- **Environment**: Windows 11, IntelliJ IDEA

## 📈 진행 현황
- [x] Phase 0: Arena 및 MemorySegment 기초 할당 성공
- [x] Phase 1: MemoryLayout 및 Padding을 활용한 데이터 정렬(Alignment) 문제 해결
- [x] Phase 2: SequenceLayout 및 VarHandle을 이용한 오프힙 구조체 배열 구현 및 인덱스 기반 접근 성공.
- [x] Phase 4: Zero-copy Slicing을 이용한 메모리 격리 및 안전한 데이터 접근 메커니즘 구현.
- [x] Phase 5: VarHandle의 CAS(Compare-And-Swap) 연산을 이용한 Lock-Free 원자적 메모리 제어 구현.
- [x] Phase 6: FileChannel.map을 활용한 Memory-Mapped File(MMAP) 기반 Zero-Copy 디스크 I/O 구현.
- [x] Phase 7: MMAP 및 CAS 결합형 Off-Heap Lock-Free 링 버퍼(Ring Buffer) 아키텍처 구현
- [x] Phase 8: 가상 스레드(Virtual Threads) 1만 개를 활용한 Shared Arena 기반 Lock-Free 링 버퍼 동시성 스트레스 테스트 완료
- [x] Phase 9: Volatile Read 및 Polling 기반 Off-Heap 실시간 데이터 Consumer 로직 구현
- [x] Phase 10: 순수 NIO ServerSocketChannel을 활용한 외부 HTTP 트래픽 수신 및 Off-Heap Queue 직접 연동
- [x] Phase 11: FileChannel transferTo 기반 Zero-Copy 네트워크 송신 구현 (sendfile 시스템 콜 활용)
- [x] Phase 12: 64-byte Cache Line Padding을 적용한 False Sharing 방지 및 하드웨어 레벨 멀티스레드 최적화 완료
- [x] Phase 13: 파이썬(Python) mmap 모듈을 활용한 이기종 언어 간(Polyglot) Zero-Copy IPC Consumer 구현
- [x] Phase 14: C++ Windows API 및 포인터 연산을 활용한 나노초(ns) 단위 HFT(고빈도 매매) Consumer 구현
- [x] Phase 15: RDBMS(Oracle 등) 병목 방어를 위한 Off-Heap 큐 기반 Write-Behind DB Flusher 및 벌크 인서트 패턴 구현
- [x] Phase 16: FFM API Downcall 적용 완료 및 100만 나노초 대의 초고속 C 엔진 결합 증명
- [x] Phase 17: MemorySegment.copy 및 MMAP을 활용한 초고속 인메모리 스냅샷(Snapshot) 덤프 및 무중단 크래시 복구(Crash Recovery) 아키텍처 구현
- [x] Phase 18: FFM API Upcall을 활용한 자바 메서드 포인터화 및 C 네이티브 엔진의 실시간 양방향 콜백(Callback) 통신 구현
- [x] Phase 19: Log4j 등 기존 로깅의 I/O 병목을 제거하는 JFR(Java Flight Recorder) 커스텀 이벤트 기반 Zero-Overhead 실시간 텔레메트리 구현
- [x] Phase 20: GC 오버헤드 및 파편화 방지를 위한 1GB 단일 Arena 기반 Zero-GC Bump-Pointer 메모리 할당기(Slab Allocator) 구현
- [x] Phase 21: 자바 객체 생성 없는 O(1) 시간 복잡도의 Off-Heap Zero-Allocation Hash Index 검색 엔진 구현 완료
- [x] Phase 22: MMAP과 해시 인덱스를 결합한 영구 저장(Persistent) 커스텀 NoSQL 데이터베이스 엔진 코어 구현
- [x] Phase 23: JDK Incubator Vector API를 활용한 오프힙 메모리 기반 SIMD(Single Instruction Multiple Data) 하드웨어 병렬 데이터 처리 엔진 구현
- [x] Phase 24: Custom ClassLoader 및 Reflection을 활용한 무중단(Zero-Downtime) 핫스왑(Hot-Swap) 엔진 구현
- [x] Phase 25: 다른 이기종 마이크로서비스와의 연동 및 독립 실행을 위한 Production JAR 패키징 완료
- [x] Phase 26: Java NIO Selector를 활용한 수만 개의 동시 연결 처리가 가능한 초저지연(Low-Latency) TCP 네트워크 게이트웨이 구현
- [x] Phase 27: Network Byte Order(Endianness) 보정 및 수신 데이터의 Off-Heap Memory 직접 동기화(Zero-Copy Ingestion) 구현
- [x] Phase 28: 1억 건 연산 테스트를 통한 CPU Cache Line Padding 성능 검증 (5.8ns/op 달성)
- [ ] Phase 29: 패딩된 시퀀스를 활용한 Zero-Contention 오프힙 링 버퍼 인덱스 관리자 이식