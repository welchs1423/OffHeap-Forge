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