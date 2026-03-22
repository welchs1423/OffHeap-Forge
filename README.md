# 🚀 OffHeap-Forge

JDK 22+의 FFM API와 Rust를 결합하여 언어의 경계를 허물고, 하드웨어의 극한 성능을 뽑아내는 초저지연 데이터 엔진입니다. 자바의 생산성과 러스트의 무자비한 성능을 공유 메모리(mmap)로 연결하여 단 1바이트의 복사도 없는 Zero-Copy 파이프라인을 구축합니다.

## 🛠 기술 스택 (Technical Stack)

### 🚀 Engine & Runtime
* **Runtime**: **JDK 25 (Early Access)**
* **Language**: **Java 25**, **Rust** (High-Speed Analyzer), **C/C++** (Native Bridge)
* **Core API**: **FFM (Foreign Function & Memory)**, **Vector API (SIMD)**, **MemoryLayout**

### 🌐 Web & Enterprise
* **Framework**: **eGovFrame 4.3 (Spring 5.3.37)**
* **Web Server**: **Apache Tomcat 9.0.115**
* **Persistence**: **MyBatis 3.5**, **DBCP2** (High-performance Connection Pool)
* **Frontend**: **Terminal-inspired Matrix UI** (JSP, JSTL, CSS3)

### 📊 Data & Infrastructure
* **Database**: **Oracle DB 21c (XE)**
* **Library**: `memmap2` (Rust), OJDBC8
* **Infrastructure**: **Docker**, **Prometheus**, **Grafana** (Full-stack Monitoring)

---

## 💡 핵심 구현 기술 (Key Implementation)

### ⚡ Extreme Performance
* **Zero-Copy Persistence**: `mmap` 기술을 활용해 디스크와 메모리를 동기화하여 가비지 컬렉션(GC) 오버헤드를 0%로 수렴시켰습니다.
* **SIMD DB Flusher**: JDK Vector API를 통해 오프힙 데이터를 256-bit 단위로 병렬 처리, Oracle DB 벌크 인서트 처리량을 극대화했습니다.
* **O(1) Shared Cursor IPC**: 자바와 러스트가 공유 메모리의 '커서'를 실시간 동기화하여, 데이터 복사 없이 상호 간의 포인터를 점프하며 읽어오는 초저지연 통신을 구현했습니다.

### 🛠️ Legacy Modernization
* **JDK 25 Bridge**: 10년 이상의 역사를 가진 eGovFrame 템플릿을 최신 **JDK 25** 환경에 이식하기 위해, 구형 라이브러리(Lombok 등)의 의존성을 제거하고 **정통 Spring DI** 방식으로 재설계했습니다.
* **Polyglot Full-Stack Connectivity**: 네트워크 오버헤드가 없는 **OS 공유 메모리(Shared Memory)**를 통해 자바-러스트-오라클-웹 UI까지 이어지는 통합 데이터 파이프라인을 구축했습니다.
* **Real-time Matrix Dashboard**: 초당 수만 건이 쏟아지는 오프힙 엔진의 상태를 별도의 복사 과정 없이 MyBatis 매퍼를 통해 실시간으로 시각화했습니다.

## 📅 업데이트 내역

### 🟧 [Season 3] Scalability & Connectivity (2026.03 ~ )
* **Phase 49**: JDK 25 기반의 Off-heap 엔진 데이터를 eGovFrame WebUI로 실시간 이식 성공 (Legacy Modernization)
* **Phase 48**: Modern Legacy - eGovFrame 4.3 프로젝트 생성 및 JDK 25 환경 이식 완료 (Lombok 제거 및 정통 Spring DI 전환)
* **Phase 47**: Prometheus & Grafana 연동을 통한 실시간 초당 처리량(TPS) 모니터링 대시보드 구축 완료
* **Phase 46**: `MERGE INTO` (Upsert) 구문을 활용한 DB Flusher 멱등성(Idempotency) 확보 및 크래시 복구 시 데이터 무결성 완벽 보장
* **Phase 45**: Chaos Engineering - Consumer(Rust) 강제 종료 및 재기동 시 데이터 유실률 0% (Zero-Downtime Hot-Reload) 아키텍처 검증 완료
* **Phase 44**: Rust 기반 컨슈머에 LLVM Auto-Vectorization(SIMD)을 활용한 4건 단위 Batch Processing 적용 및 누락 방지 로직 보완
* **Phase 43**: 1:N Polyglot(Rust, Python) 다중 컨슈머 아키텍처 및 Rust 기반 JSON 감사 로그 시스템 구축
* **Phase 42**: OS 하드웨어 틱(Tick)을 활용한 마이크로초(μs) 단위 초정밀 DB 인서트 프로파일러 장착
* **Phase 41**: JDK FFM API 기반 C/C++ Native Bridge 연결 및 OS 초정밀 하드웨어 타이머(DLL) 연동 완료
* **Phase 40**: Shared Memory 기반 Java ↔ Rust 양방향 Zero-Copy IPC (피드백 채널) 구축
* **Phase 39**: Rust 측 실시간 데이터 필터링 및 임계치 감지(Alerting) 로직 완성
* **Phase 38**: Shared Cursor(Head/Tail) 동기화를 통한 **O(1) Jump** 조회 최적화 구현
* **Phase 37**: `memmap2` 라이브러리를 활용한 Rust 기반 Zero-Copy 메모리 리더 구축
* **Phase 36**: JDK Vector API(SIMD) 기반 Oracle DB 비동기 Bulk Flusher 파이프라인 완성

### 🟩 [Season 2] Operational Excellence (2025.12 ~ 2026.02)
* **Phase 35**: mmap 기반 **Phoenix Recovery**(자가 복구) 로직 및 데이터 영속성 검증
* **Phase 34**: 67,130 TPS 도달 시의 네트워크 병목 지점 파악 및 최적화 리포트 작성
* **Phase 33**: Grafana 커스텀 대시보드 구축 및 실시간 트래픽 가시성 확보
* **Phase 32**: Prometheus 규격 메트릭 익스포터 구축 및 엔드포인트(/metrics) 활성화
* **Phase 31**: 오프힙 데이터 스냅샷 및 증분 백업 시스템 프로토타입 제작
* **Phase 30**: 운영 환경 모니터링을 위한 Docker-Compose 기반 인프라 스택 구성

### 🟦 [Season 1] The Core of Speed (2025.09 ~ 2025.11)
* **Phase 29**: Java NIO Selector 기반 초저지연 비동기 TCP 게이트웨이 완공
* **Phase 28**: **5.7ns** Latency 달성 및 Atomic 연산 성능 벤치마크 완료
* **Phase 27**: 64-byte Cache Line Padding 적용으로 False Sharing 원천 차단
* **Phase 26**: CAS(Compare-And-Swap) 기반 무경합(Lock-Free) 인덱스 관리자 구현
* **Phase 25**: 고속 메모리 재사용을 위한 슬래브 할당기(Slab Allocator) 알고리즘 적용
* **Phase 24**: MemoryLayout 기반 하드웨어 정렬(Alignment) 데이터 구조체 설계
* **Phase 23**: VarHandle 기반 메모리 직접 접근 및 Volatile 가시성 확보
* **Phase 22**: Arena 기반 오프힙 메모리 생명주기 관리 및 자원 누수 방지 로직 구축
* **Phase 21**: Native 메모리 직접 할당(malloc) 및 세그먼트 경계 보호 로직 구현
* **Phase 20**: 매뉴얼 메모리 해제를 통한 Zero-GC(가비지 컬렉터 무력화) 환경 달성
* **Phase 19**: FFM API 기반 메모리 주소 역참조 및 포인터 연산 안정화
* **Phase 18**: 하드웨어 최적화를 위한 Little-Endian 바이트 순서 고정 로직 적용
* **Phase 17**: MemorySegment 슬라이싱을 통한 대용량 데이터 파티셔닝 기술 구현
* **Phase 16**: JMH(Java Microbenchmark Harness) 기반 성능 측정 프레임워크 도입
* **Phase 15**: 싱글 스레드 환경에서의 메모리 처리량(Throughput) 한계 측정
* **Phase 14**: 오프힙 저장 전용 데이터 모델(Schema) 정의 및 직렬화 제거
* **Phase 13**: Memory-mapped file I/O 프로토타입 제작 및 디스크 동기화 테스트
* **Phase 12**: FFM API를 활용한 외부 C 네이티브 라이브러리 링크 기술 검증
* **Phase 11**: 가상 메모리 주소 계산을 위한 커스텀 오프셋 계산 로직 구축
* **Phase 10**: 멀티스레드 동시 접근 시의 데이터 무결성 테스트 시나리오 수행
* **Phase 09**: Native 메모리 영역에서의 원자적 연산(Atomic) 프로토콜 설계
* **Phase 08**: 고효율 데이터 수신을 위한 오프힙 버퍼 풀링(Pooling) 시스템 구축
* **Phase 07**: 오프힙 기반 원형 큐(Ring Buffer) 개념 증명 및 설계 완료
* **Phase 06**: Java 22 FFM(Foreign Function & Memory) API 개발 환경 세팅
* **Phase 05**: Direct ByteBuffer와 MemorySegment 간 성능 비교 분석
* **Phase 04**: 엔진 코어 아키텍처 및 넌블로킹(Non-blocking) 데이터 흐름 설계
* **Phase 03**: 나노초(Nanosecond) 단위 처리 성능 목표 및 KPI 설정
* **Phase 02**: CPU 캐시 라인 크기 감지 및 하드웨어 가속 전략 수립
* **Phase 01**: OffHeap-Forge 프로젝트 초기화 및 기본 클래스 구조 생성

---

## 🚀 How to Run

엔진의 정상 작동을 위해 Java 엔진을 먼저 가동한 후 Rust 컨슈머를 실행해야 합니다.

### 1. Prerequisites
* **Oracle DB**: `system/oracle` 계정으로 접속 가능한 상태여야 함
* **Rust**: `cargo` 명령어가 환경 변수에 등록되어 있어야 함

### 2. Terminal 1: Java Main Engine
```powershell
# Compile
javac --add-modules jdk.incubator.vector src/ForgeMain.java

# Run
java -cp "src;.;ojdbc8.jar" --add-modules jdk.incubator.vector ForgeMain
```

### 3. Terminal 2: Rust High-Speed Consumer
```powershell
cd pipeline-rust
cargo run
```

### 4. Terminal 3: Traffic Injection (Testing)
```powershell
$client = New-Object System.Net.Sockets.TcpClient("127.0.0.1", 9999);
$stream = $client.GetStream();
$stream.Write([BitConverter]::GetBytes([long]99999), 0, 8);
$client.Close();
```

### 5. Web Dashboard (eGovFrame)
* **IDE**: IntelliJ IDEA (with Smart Tomcat Plugin)
* **Access**: `http://localhost:8080/egovSampleList.do`
* **Note**: 엔진이 가동 중이어야 실시간 데이터를 확인할 수 있습니다.