# 🚀 OffHeap-Forge: High-Performance Data Engine

JDK 22+의 **Foreign Function & Memory (FFM) API**를 활용하여 자바의 성능 한계에 도전하고, 하드웨어의 물리적 특성을 소프트웨어 아키텍처로 승화시킨 초저지연 데이터 엔진입니다.

## 🎯 Project Core Essence
> **자바의 가비지 컬렉션(GC)을 우회하고, CPU 캐시 라인을 직접 제어하며, 네트워크 트래픽을 나노초 단위로 처리합니다.**

---

## 📈 Performance & Metrics
시즌 1(속도)과 시즌 2(안정성)를 거치며 달성한 핵심 기술 지표입니다.

| Category | Performance | Technical Insight |
| :--- | :--- | :--- |
| **Update Latency** | **5.7ns / op** | 100M Atomic Updates (Phase 28) |
| **Throughput** | **67,130+ TPS** | PowerShell TCP Client Limit Reach |
| **GC Overhead** | **0ms (Zero-GC)** | Slab Allocator & Off-Heap Memory |
| **Durability** | **Zero-Copy** | mmap based Crash Recovery |

---

## 🏗️ System Architecture
데이터 유입부터 영구 저장까지 **경합 제로(Zero-Contention)** 파이프라인으로 설계되었습니다.

1. **Ingestion Layer**: Java NIO Selector 기반의 초저지연 비동기 TCP 게이트웨이.
2. **Buffer Layer**: FFM API 기반의 Memory-Mapped Off-Heap 링 버퍼.
3. **Execution Layer**: Cache-Line Padding이 적용된 무경합 인덱스 관리자.
4. **Observability Layer**: 메인 로직 간섭 없는 Zero-Overhead 텔레메트리 (Prometheus).
5. **Persistence Layer**: mmap 기술을 활용한 디스크 물아일체 저장 및 자가 복구.
6. **Integration Layer (New)**: Vector API(SIMD) 기반 RDBMS 초고속 비동기 벌크 플러셔.

---

## 🛠️ Project Journey: Season 1 ~ 3

### 🟦 [Season 1] The Core of Speed (Phase 0 ~ 29)
**목표: 자바의 한계를 부수는 극한의 로우 레벨 최적화 및 엔진 기초 구축**
* **Memory Management**: Arena 할당, MemoryLayout 정렬, Zero-GC Slab Allocator 구현 완료.
* **Hardware & Concurrency**: CAS 기반 Lock-Free 제어 및 64-byte Cache Line Padding 적용 (**5.7ns 달성**).
* **Networking & Gateway**: Java NIO Selector 기반 초저지연 TCP 게이트웨이 완공.

### 🟩 [Season 2] Operational Excellence (Phase 30 ~ 35)
**목표: 실전 운영을 위한 실시간 관측(Monitoring) 및 영속성(Persistence) 확보**
* **Observability**: Prometheus 규격 메트릭 익스포터 구축 및 Grafana 실시간 모니터링 연동 (**67K TPS** 성능 검증).
* **Persistence**: Memory-Mapped File (mmap) 기술을 이용한 디스크 물아일체 저장 및 **Phoenix Recovery**(자가 복구) 로직 완성.

### 🟧 [Season 3] Scalability & Connectivity (Phase 36 ~ )
**목표: 단일 노드를 넘어선 영토 확장 및 폴리글랏 생태계 구축**
* **Database Integration**
  * [x] **Phase 36**: JDK Vector API(SIMD) 기반 오프힙 메모리 초고속 싹쓸이 및 Oracle DB 벌크 인서트(Batch) 비동기 파이프라인 구축 완료.

---

## 🚀 Getting Started

### 1. 엔진 및 모니터링 스택 가동
```bash
# 자바 엔진 실행 (Vector API 활성화 및 JDBC 드라이버 포함)
javac --add-modules jdk.incubator.vector ForgeMain.java
java -cp ".;ojdbc8.jar" --add-modules jdk.incubator.vector ForgeMain

# Docker 모니터링 스택 실행 (Prometheus, Grafana)
docker-compose up -d
```

### 2. 스트레스 테스트 (PowerShell)
```powershell
$client = New-Object System.Net.Sockets.TcpClient("127.0.0.1", 9999);
$stream = $client.GetStream();
1..8 | ForEach-Object {
    $data = [BitConverter]::GetBytes([long]($_ * 7777));
    $stream.Write($data, 0, $data.Length);
}
$client.Close();
```

---

## 🌊 Roadmap for Season 3 (Next Steps)
- **Polyglot Expansion**: Rust/Go 언어와의 Zero-Copy IPC 브릿지 완성.
- **Cluster Replication**: 다중 노드 간 오프힙 데이터 미러링 구현.