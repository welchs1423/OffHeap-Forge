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

---

## 🛠️ Project Journey: Season 1 & 2

### 🟦 [Season 1] The Core of Speed (Phase 0 ~ 29)
**목표: 자바의 한계를 부수는 극한의 로우 레벨 최적화 및 엔진 기초 구축**

* **Memory Management**
    * [x] Phase 0~4: Arena 할당, MemoryLayout 정렬 및 Zero-copy Slicing 성공
    * [x] Phase 20: 1GB 단일 Arena 기반 Zero-GC Slab Allocator 구현
    * [x] Phase 21: 자바 객체 생성 없는 O(1) Off-Heap Zero-Allocation 해시 검색 엔진
* **Hardware & Concurrency**
    * [x] Phase 5~8: VarHandle CAS 기반 Lock-Free 원자적 제어 및 가상 스레드 테스트
    * [x] Phase 12, 28: 64-byte Cache Line Padding 적용, False Sharing 방지 (**5.7ns 달성**)
    * [x] Phase 23: JDK Vector API 활용 SIMD 하드웨어 병렬 데이터 처리 엔진
* **Networking & Gateway**
    * [x] Phase 10~11: NIO ServerSocketChannel 및 transferTo 기반 Zero-Copy 송수신
    * [x] Phase 26~29: Java NIO Selector 기반 초저지연 TCP 게이트웨이 및 통합 엔진 완공
* **Polyglot Integration**
    * [x] Phase 13~14: Python mmap IPC 및 C++ 네이티브 HFT Consumer 연동
    * [x] Phase 15~16: Oracle DB Write-Behind Flusher 및 FFM Downcall 증명
    * [x] Phase 24~25: Zero-Downtime 핫스왑 엔진 및 Production JAR 패키징 완료

### 🟩 [Season 2] Operational Excellence (Phase 30 ~ 35)
**목표: 실전 운영을 위한 실시간 관측(Monitoring) 및 영속성(Persistence) 확보**

* **Observability & Telemetry**
    * [x] Phase 30: Prometheus 규격 메트릭 익스포터 구축
    * [x] Phase 31: Grafana 대시보드 시각화 및 실시간 모니터링 연동
    * [x] Phase 32: Observability 아키텍처 완성 및 **67K TPS** 성능 검증
* **Persistence & Reliability**
    * [x] Phase 33~34: Memory-Mapped File (mmap) 기술을 이용한 디스크 물아일체 저장 구현
    * [x] Phase 35: **Phoenix Recovery** - 디스크 바이너리 스캔 기반 자동 상태 복구 로직 완성

---

## 🚀 Getting Started

### 1. 엔진 및 모니터링 스택 가동
```bash
# 자바 엔진 실행 (9999: Data, 9090: Metrics)
javac ForgeMain.java
java ForgeMain

# Docker 모니터링 스택 실행 (Prometheus, Grafana)
docker-compose up -d
```

### 2. 스트레스 테스트 (PowerShell)
```powershell
$client = New-Object System.Net.Sockets.TcpClient("127.0.0.1", 9999);
$stream = $client.GetStream();
$i = 0;
while ($true) {
    $i++;
    $data = [BitConverter]::GetBytes([long]$i);
    $stream.Write($data, 0, $data.Length);
}
```
**Grafana Dashboard**: `http://localhost:3000` (admin/admin) 접속 시 실시간 TPS 산맥 확인 가능.

---

## 🌊 Roadmap for Season 3
- **Vectorized DB Flusher**: SIMD API를 활용한 DB 벌크 동기화 최적화.
- **Polyglot Expansion**: Rust/Go 언어와의 Zero-Copy IPC 브릿지 완성.
- **Cluster Replication**: 다중 노드 간 오프힙 데이터 미러링 구현.