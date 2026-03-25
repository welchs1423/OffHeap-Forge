use std::io::Write;
use std::net::TcpStream;
use std::thread;
use std::time::Duration;

fn main() {
    println!("🚀 [Rust Factory] 불사조(Auto-Reconnect) 모드 가동!");

    let mut seq = 1u64;

    // 1. 바깥쪽 무한 루프 (죽어도 다시 살아남)
    loop {
        println!("⏳ Java 엔진(Port 9999)으로 연결을 시도합니다...");

        match TcpStream::connect("127.0.0.1:9999") {
            Ok(mut stream) => {
                println!("🔌 [Network] Java 엔진과 TCP 연결 성공! 데이터 폭격 시작!");

                // 2. 안쪽 루프 (정상적일 때 데이터 폭격)
                loop {
                    let val: u64 = 50000 + ((seq.wrapping_mul(137) % 150) as u64);
                    let bytes = val.to_le_bytes();

                    // 자바가 꺼져서 전송에 실패하면 안쪽 루프를 깨고 바깥쪽(재접속)으로 나감!
                    if let Err(e) = stream.write_all(&bytes) {
                        println!("⚠️ 전송 실패 (자바가 꺼진 듯?): {}", e);
                        break;
                    }

                    seq += 1;
                    thread::sleep(Duration::from_millis(10));
                }
            }
            Err(e) => {
                println!("❌ 연결 실패 (자바 켜는 중...?): {}. 3초 뒤 재시도...", e);
                thread::sleep(Duration::from_secs(3));
            }
        }
    }
}