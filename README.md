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
- [x] Phase 0: Arena 및 MemorySegment를 이용한 기초적인 오프힙 메모리 할당 및 접근 성공.
- [ ] Phase 1: 고성능 오프힙 데이터 구조(Off-heap Queue/Map) 설계.