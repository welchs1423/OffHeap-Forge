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
            <tbody>
                <c:forEach var="result" items="${resultList}" varStatus="status">
                    </c:forEach>
            </tbody>
            
            <tbody id="liveDataBody">
                <c:forEach var="result" items="${resultList}" varStatus="status">
                    </c:forEach>
            </tbody>
        </table>
    </div>
</body>
<script>
    // 🔥 Phase 50: 1초(1000ms)마다 백엔드를 찌르는 실시간 모터
    setInterval(function() {
        
        // 1. 어제 뚫어놓은 API로 몰래 접속해서 데이터를 가져옴
        fetch('/api/forge/liveData.do')
            .then(response => response.json()) // JSON 형태로 번역
            .then(data => {
                // 2. 아까 이름표 달아둔 <tbody> 구역을 찾음
                const tbody = document.getElementById('liveDataBody');
                
                // 3. 기존에 있던 화면 데이터를 싹 지워버림 (새로고침 효과)
                tbody.innerHTML = ''; 

                // 4. 만약 DB에 데이터가 하나도 없다면?
                if (data.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:#999;">📭 데이터를 기다리는 중... (Rust 엔진을 켜주세요!)</td></tr>';
                    return;
                }

                // 5. 가져온 최신 데이터들을 한 줄(tr)씩 화면에 꽂아 넣기
                data.forEach(item => {
                    const tr = document.createElement('tr');
                    
                    // 테이블 컬럼 순서에 맞게 데이터를 넣어줍니다. (예시: seqId, metricVal)
                    tr.innerHTML = 
                        '<td align="center" class="listtd">' + item.seqId + '</td>' +
                        '<td align="center" class="listtd">' + item.metricVal + '</td>' +
                        '<td align="center" class="listtd">실시간 🟢</td>' +
                        '<td align="center" class="listtd">SYSTEM</td>';
                        
                    tbody.appendChild(tr);
                });
            })
            .catch(error => console.error('통신 에러가 발생했습니다:', error));
            
    }, 1000); // 1000밀리초 = 1초마다 무한 반복!
</script>
</html>