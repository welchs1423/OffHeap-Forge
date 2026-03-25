use std::io::Write;
use std::net::TcpStream;
use std::thread;
use std::time::Duration;

fn main() -> std::io::Result<()> {
    println!("🚀 [Rust Factory] 가동 준비 완료!");
    println!("⏳ Java 엔진(Port 9999)으로 연결을 시도합니다...");

    // 1. 자바가 열어둔 9999 포트로 쳐들어갑니다!
    let mut stream = TcpStream::connect("127.0.0.1:9999")?;
    println!("🔌 [Network] Java 엔진과 TCP 연결 성공! 데이터 폭격을 시작합니다!");

    let mut seq = 1u64;

    loop {
        // 2. 가짜 데이터(숫자)를 미친 듯이 생성합니다. (50000 ~ 50100 사이 뺑뺑이)
        let val: u64 = 50000 + (seq % 100);

        // 3. 자바가 좋아하는 Little Endian 8바이트로 포장합니다.
        let bytes = val.to_le_bytes();

        // 4. 자바(9999 포트)로 냅다 던집니다!
        if let Err(e) = stream.write_all(&bytes) {
            println!("⚠️ 전송 실패 (자바가 꺼졌나요?): {}", e);
            break;
        }

        seq += 1;

        // 너무 빠르면 화면에서 보기 힘드니까 0.01초(10ms)마다 쏩니다. (초당 100개!)
        thread::sleep(Duration::from_millis(10));
    }

    Ok(())
}