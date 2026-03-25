<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>OffHeap-Forge Monitor</title>
    <style>
        body { background: #000; color: #00ff41; font-family: 'Consolas', monospace; padding: 50px; }
        .monitor { border: 2px solid #00ff41; padding: 20px; border-radius: 10px; box-shadow: 0 0 20px rgba(0,255,65,0.3); }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th { border-bottom: 1px solid #00ff41; text-align: left; padding: 10px; color: #fff; }
        td { padding: 10px; border-bottom: 1px solid #222; }
        .status { color: #ffcf00; animation: blink 1s infinite; }
        @keyframes blink { 50% { opacity: 0; } }
    </style>
</head>
<body>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<div style="width: 100%; height: 200px; margin-bottom: 20px;">
    <canvas id="liveChart"></canvas>
</div>
    <div class="monitor">
        <h1>🚀 OFF-HEAP FORGE REAL-TIME MONITOR</h1>
        <p>SYSTEM STATUS: <span class="status">RUNNING ON JDK 25</span></p>
        <hr border="1" color="#00ff41">
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
</body>
<script>
    const ctx = document.getElementById('liveChart').getContext('2d');
    const liveChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: '⚡ 실시간 트래픽 파동 (Rust -> Java -> Oracle)',
                data: [],
                borderColor: 'rgba(0, 255, 136, 1)', // 사이버펑크 네온 그린!
                backgroundColor: 'rgba(0, 255, 136, 0.1)',
                borderWidth: 2,
                tension: 0.3, // 부드러운 곡선
                pointRadius: 0, // 데이터가 많으니 점은 숨김
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            animation: false, // 1초마다 번쩍거리는 걸 막기 위해 애니메이션 OFF
            scales: {
                x: { display: false }, // X축 숫자 숨기기
                y: {
                    suggestedMin: 49950, // 러스트가 50000~50100을 쏘므로 범위 고정
                    suggestedMax: 50150
                }
            }
        }
    });

    setInterval(function() {
        fetch('/api/forge/liveData.do?_=' + new Date().getTime())
            .then(response => response.json())
            .then(data => {
                const tbody = document.getElementById('liveDataBody');
                tbody.innerHTML = '';

                if (data.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="3" style="text-align:center;">📭 데이터를 기다리는 중...</td></tr>';
                    return;
                }

                // 🔥 [차트 갱신 로직]
                const chartLabels = data.map(item => item.seqId).reverse();
                const chartValues = data.map(item => item.metricVal).reverse();

                liveChart.data.labels = chartLabels;
                liveChart.data.datasets[0].data = chartValues;
                liveChart.update(); // 차트 다시 그리기 샷!

                // 🔥 [기존 표 갱신 로직] 레이아웃 3칸으로 완벽 수정!
                data.forEach(item => {
                    const tr = document.createElement('tr');
                    tr.innerHTML =
                        '<td align="center" class="listtd">' + item.seqId + '</td>' +
                        '<td align="center" class="listtd"><strong>' + item.metricVal + '</strong></td>' +
                        '<td align="center" class="listtd" style="color:green;">Live 🟢 SYSTEM</td>';
                    tbody.appendChild(tr);
                });
            })
            .catch(error => console.error('통신 에러:', error));
    }, 100);
</script>
</html>