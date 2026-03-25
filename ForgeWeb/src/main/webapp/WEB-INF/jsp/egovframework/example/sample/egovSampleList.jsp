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
        .tps-badge { font-size: 28px; color: #ff003c; font-weight: bold; text-shadow: 0 0 10px #ff003c; display: flex; align-items: center; gap: 20px; }

        /* 🔥 일시정지 버튼 스타일 */
        .btn-pause { background: #ff003c; color: white; border: 1px solid white; padding: 8px 15px; font-weight: bold; cursor: pointer; border-radius: 5px; font-family: 'Consolas', monospace; font-size: 16px; transition: 0.3s; }
        .btn-pause.paused { background: #00ff41; color: black; border: 1px solid black; box-shadow: 0 0 10px #00ff41; }

        /* 🔥 하단 패널 레이아웃 (터미널 + 랭킹) */
        .panel-container { display: flex; gap: 20px; margin-bottom: 20px; }
        .alert-terminal { flex: 2; height: 180px; background: #0a0a0a; border: 1px solid #ff003c; overflow-y: hidden; padding: 10px; box-shadow: inset 0 0 10px rgba(255,0,60,0.2); }
        .top-ranking { flex: 1; height: 180px; background: #0a0a0a; border: 1px solid #ffcf00; padding: 10px; box-shadow: inset 0 0 10px rgba(255,207,0,0.2); overflow-y: hidden; }

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
        <div class="tps-badge">
            <button id="pauseBtn" class="btn-pause" onclick="togglePause()">⏸️ PAUSE LIVE</button>
            <span id="tpsCounter">0 TPS</span>
        </div>
    </div>
    <hr border="1" color="#00ff41">

    <div style="width: 100%; height: 250px; margin-bottom: 20px;">
        <canvas id="liveChart"></canvas>
    </div>

    <div class="panel-container">
        <div class="alert-terminal" id="alertBox">
            <div style="color: #777; font-size: 12px; margin-bottom: 10px;">> ALERT LOGGING INITIALIZED... WAITING FOR SPIKES...</div>
        </div>

        <div class="top-ranking" id="topSpikesBox">
            <div style="color: #ffcf00; font-weight: bold; margin-bottom: 10px; font-size: 16px; text-shadow: 0 0 5px #ffcf00;">🏆 SESSION TOP 5 SPIKES</div>
            </div>
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
                tension: 0.1,
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

    let lastSeqId = 0;
    let lastFetchTime = Date.now();
    let lastAlertedSeq = 0;

    // 🔥 Phase 55 핵심 상태 변수
    let isPaused = false;
    let topSpikesList = [];

    // ⏸️ 일시정지 스위치 함수
    function togglePause() {
        isPaused = !isPaused;
        const btn = document.getElementById('pauseBtn');
        if (isPaused) {
            btn.innerText = '▶️ RESUME LIVE';
            btn.classList.add('paused');
        } else {
            btn.innerText = '⏸️ PAUSE LIVE';
            btn.classList.remove('paused');
        }
    }

    setInterval(function() {
        // 🔥 화면이 멈춤(Paused) 상태면 서버에 요청 안 하고 통과!
        if (isPaused) return;

        fetch('/api/forge/liveData.do?_=' + new Date().getTime())
            .then(response => response.json())
            .then(data => {
                const tbody = document.getElementById('liveDataBody');
                const alertBox = document.getElementById('alertBox');
                const topBox = document.getElementById('topSpikesBox');
                tbody.innerHTML = '';

                if (data.length === 0) return;

                // [TPS 계산]
                const currentMaxSeq = Math.max(...data.map(item => item.seqId));
                const now = Date.now();
                if (lastSeqId > 0 && currentMaxSeq > lastSeqId) {
                    const deltaSeq = currentMaxSeq - lastSeqId;
                    const deltaMs = now - lastFetchTime;
                    const tps = Math.round((deltaSeq / deltaMs) * 1000);
                    document.getElementById('tpsCounter').innerText = tps + ' TPS';
                }
                lastSeqId = currentMaxSeq;
                lastFetchTime = now;

                // [차트 갱신]
                const chartLabels = data.map(item => item.seqId).reverse();
                const chartValues = data.map(item => item.metricVal).reverse();
                liveChart.data.labels = chartLabels;
                liveChart.data.datasets[0].data = chartValues;
                liveChart.update();

                // 🔥 [TOP 5 랭킹 추출 & 렌더링 로직]
                data.forEach(item => {
                    // 중복 방지: 이미 랭킹에 있는 번호는 무시
                    if (!topSpikesList.some(x => x.seqId === item.seqId)) {
                        topSpikesList.push({ seqId: item.seqId, val: item.metricVal });
                    }
                });
                // 값 기준으로 내림차순 정렬 후 상위 5개만 컷!
                topSpikesList.sort((a, b) => b.val - a.val);
                topSpikesList = topSpikesList.slice(0, 5);

                // 금색 전광판 업데이트 (JSP 에러 방지용 문자열 조합)
                topBox.innerHTML = '<div style="color: #ffcf00; font-weight: bold; margin-bottom: 10px; font-size: 16px; text-shadow: 0 0 5px #ffcf00;">🏆 SESSION TOP 5 SPIKES</div>';
                topSpikesList.forEach((item, index) => {
                    topBox.innerHTML += '<div style="color: #fff; margin: 8px 0; font-size: 15px;">' +
                                        '<span style="color:#888;">' + (index + 1) + '.</span> SEQ: ' + item.seqId +
                                        ' <span style="color:#ffcf00; float:right; font-weight:bold;">VAL: ' + item.val + '</span></div>';
                });

                // [Alert 경보 터미널]
                const newItems = data.filter(item => item.seqId > lastAlertedSeq).reverse();
                newItems.forEach(item => {
                    if (item.metricVal >= 50130) {
                        const p = document.createElement('div');
                        p.className = 'alert-text';
                        p.innerText = '[CRITICAL] SEQ: ' + item.seqId + ' - 🔥 OVERLOAD SPIKE DETECTED (VAL: ' + item.metricVal + ')';
                        alertBox.prepend(p);
                        if (alertBox.children.length > 20) {
                            alertBox.removeChild(alertBox.lastChild);
                        }
                    }
                    lastAlertedSeq = Math.max(lastAlertedSeq, item.seqId);
                });

                // [표 갱신]
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