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
                <c:forEach var="forge" items="${forgeList}">
                    <tr>
                        <td># ${forge.seqId}</td>
                        <td><strong>${forge.metricVal}</strong></td>
                        <td><span style="color:#00ff41">OK</span></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</body>
</html>