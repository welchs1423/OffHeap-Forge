<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>OffHeap-Forge Monitor</title>
    <style>
        body { background: #000; color: #00ff41; font-family: 'Consolas', monospace; padding: 50px; }
        .monitor { border: 2px solid #00ff41; padding: 20px; border-radius: 10px; box-shadow: 0 0 20px rgba(0,255,65,0.3); }
        .header-panel { display: flex; justify-content: space-between; align-items: center; }
        .tps-badge { font-size: 28px; color: #ff003c; font-weight: bold; text-shadow: 0 0 10px #ff003c; }
        .alert-terminal { height: 150px; background: #0a0a0a; border: 1px solid #ff003c; overflow-y: hidden; padding: 10px; margin-bottom: 20px; box-shadow: inset 0 0 10px rgba(255,0,60,0.2); }
        .alert-text { color: #ff003c; margin: 5px 0; font-size: 14px; text-shadow: 0 0 5px #ff003c; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th { border-bottom: 1px solid #00ff41; text-align: left; padding: 10px; color: #fff; }
        td { padding: 10px; border-bottom: 1px solid #222; }
        .status { color: #ffcf00; animation: blink 1s infinite; }
        @keyframes blink { 50% { opacity: 0; } }
    </style>
</head>
<body>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<div class="monitor">
    <div class="header-panel">
        <div>
            <h1>🚀 OFF-HEAP FORGE REAL-TIME MONITOR</h1>
            <p>SYSTEM STATUS: <span class="status">RUNNING ON JDK 25</span></p>
        </div>
        <div class="tps-badge" id="tpsCounter">0 TPS</div>
    </div>
    <hr border="1" color="#00ff41">

    <div style="width: 100%; height: 250px; margin-bottom: 20px;">
        <canvas id="liveChart"></canvas>
    </div>

    <div class="alert-terminal" id="alertBox">
        <div style="color: #777; font-size: 12px; margin-bottom: 10px;">> ALERT LOGGING INITIALIZED... WAITING FOR SPIKES...</div>
    </div>

    <table>
        <thead>
            <tr>
                <th>SEQ_ID</th>
                <th>METRIC_VALUE</th>
                <th>ENGINE_STATUS</th>
            </tr>
        </thead>
        <tbody id="liveDataBody">
            <c:forEach var="result" items="${resultList}" varStatus="status">
            </c:forEach>
        </tbody>
    </table>
</div>

<script>
    // 1. 차트 기본 세팅
    const ctx = document.getElementById('liveChart').getContext('2d');
    const liveChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: '⚡ 실시간 트래픽 파동 (Rust -> Java -> Oracle)',
                data: [],
                borderColor: 'rgba(0, 255, 136, 1)',
                backgroundColor: 'rgba(0, 255, 136, 0.1)',
                borderWidth: 2,
                tension: 0.1, // 스파이크를 더 날카롭게!
                pointRadius: 0,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            animation: false,
            scales: {
                x: { display: false },
                y: { suggestedMin: 49950, suggestedMax: 50150 }
            }
        }
    });

    // 2. TPS 및 Alert 계산용 변수
    let lastSeqId = 0;
    let lastFetchTime = Date.now();
    let lastAlertedSeq = 0;

    // 3. 0.1초(100ms)마다 구동되는 메인 모터
    setInterval(function() {
        fetch('/api/forge/liveData.do?_=' + new Date().getTime())
            .then(response => response.json())
            .then(data => {
                const tbody = document.getElementById('liveDataBody');
                const alertBox = document.getElementById('alertBox');
                tbody.innerHTML = '';

                if (data.length === 0) return;

                // 🔥 [TPS 계산 엔진] 이전 갱신 때보다 시퀀스가 얼마나 증가했는지로 초당 처리량(TPS) 도출
                const currentMaxSeq = Math.max(...data.map(item => item.seqId));
                const now = Date.now();

                if (lastSeqId > 0 && currentMaxSeq > lastSeqId) {
                    const deltaSeq = currentMaxSeq - lastSeqId;
                    const deltaMs = now - lastFetchTime;
                    // 1초(1000ms) 기준으로 환산
                    const tps = Math.round((deltaSeq / deltaMs) * 1000);
                    document.getElementById('tpsCounter').innerText = tps + ' TPS';
                }
                lastSeqId = currentMaxSeq;
                lastFetchTime = now;

                // 🔥 [차트 갱신]
                const chartLabels = data.map(item => item.seqId).reverse();
                const chartValues = data.map(item => item.metricVal).reverse();

                liveChart.data.labels = chartLabels;
                liveChart.data.datasets[0].data = chartValues;
                liveChart.update();

                // 🔥 [Alert 경보 터미널] 50130 이상이면 해킹(?) 경고 로그 투척!
                // 중복 경고 방지를 위해 마지막으로 경고한 번호 이후의 새 데이터만 검사
                const newItems = data.filter(item => item.seqId > lastAlertedSeq).reverse();
                newItems.forEach(item => {
                    if (item.metricVal >= 50130) {
                        const p = document.createElement('div');
                        p.className = 'alert-text';
                        p.innerText = '[CRITICAL] SEQ: ' + item.seqId + ' - 🔥 OVERLOAD SPIKE DETECTED (VAL: ' + item.metricVal + ')';
                        alertBox.prepend(p); // 최신 로그가 맨 위에 쌓이게

                        // 터미널 박스 안에 로그가 너무 많아지면 맨 아래꺼 삭제 (20줄 유지)
                        if (alertBox.children.length > 20) {
                            alertBox.removeChild(alertBox.lastChild);
                        }
                    }
                    lastAlertedSeq = Math.max(lastAlertedSeq, item.seqId);
                });

                // 🔥 [표 갱신]
                data.forEach(item => {
                    const tr = document.createElement('tr');
                    tr.innerHTML =
                        '<td align="center">' + item.seqId + '</td>' +
                        '<td align="center"><strong>' + item.metricVal + '</strong></td>' +
                        '<td align="center" style="color:green;">Live 🟢 SYSTEM</td>';
                    tbody.appendChild(tr);
                });
            })
            .catch(error => console.error('통신 에러:', error));
    }, 100);
</script>
</body>
</html>