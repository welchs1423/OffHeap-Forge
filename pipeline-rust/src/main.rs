use memmap2::MmapOptions;
use std::fs::OpenOptions;
use std::io::Write; // 파일 쓰기용
use std::thread;
use std::time::{Duration, SystemTime, UNIX_EPOCH};

fn main() -> Result<(), Box<dyn std::error::Error>> {
    println!("🦀 [Rust Analyzer] Two-Way IPC & Audit Logger Online!");

    let file = OpenOptions::new().read(true).open("../forge-data.dat")?;
    let mmap = unsafe { MmapOptions::new().map(&file)? };

    let fb_file = OpenOptions::new().read(true).write(true).open("../forge-feedback.dat")?;
    let mut fb_mmap = unsafe { MmapOptions::new().map_mut(&fb_file)? };

    // 🚀 [Phase 43] 감사 로그(Audit Log) 파일 열기 (없으면 만들고, 있으면 이어쓰기)
    let mut log_file = OpenOptions::new().create(true).append(true).open("../alert_audit.log")?;

    let cursor_offset = 1024 * 8;
    let mut last_read_idx: u64 = 0;

    let mut total_count = 0u64;
    let mut alert_count = 0u64;

    loop {
        let tail_data = &mmap[cursor_offset..cursor_offset + 8];
        let current_java_tail = u64::from_le_bytes(tail_data.try_into()?);

if current_java_tail > last_read_idx {
            let available = current_java_tail - last_read_idx;

            // 🚀 [Phase 44] Rust SIMD(Auto-Vectorized) 4건 Batch Processing
            if available >= 4 {
                for _ in 0..(available / 4) {
                    let target_idx = last_read_idx % 1024;

                    // Ring Buffer 경계선(Wrap-around)을 넘지 않는 안전한 구간일 때만 4개 한꺼번에 처리
                    if target_idx <= 1024 - 4 {
                        let offset = (target_idx * 8) as usize;
                        // 8바이트 * 4개 = 32바이트를 한 번에 메모리에서 긁어옵니다. (LLVM SIMD 발동!)
                        let chunk = &mmap[offset..offset + 32];

                        let val1 = u64::from_le_bytes(chunk[0..8].try_into().unwrap());
                        let val2 = u64::from_le_bytes(chunk[8..16].try_into().unwrap());
                        let val3 = u64::from_le_bytes(chunk[16..24].try_into().unwrap());
                        let val4 = u64::from_le_bytes(chunk[24..32].try_into().unwrap());

                        let batch = [val1, val2, val3, val4];

                        for &val in &batch {
                            total_count += 1;
                            if val >= 50000 {
                                alert_count += 1;
                                println!("🚨 [ALERT #{}] High Value: {} (Total: {})", alert_count, val, total_count);
                                fb_mmap[0..8].copy_from_slice(&alert_count.to_le_bytes());
                                let timestamp = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_millis();
                                let log_json = format!(r#"{{"timestamp": {}, "alert_id": {}, "value": {}}}"#, timestamp, alert_count, val);
                                writeln!(log_file, "{}", log_json).unwrap();
                            } else {
                                println!("✅ [Normal] Value: {} (Total: {})", val, total_count);
                            }
                        }
                        println!("⚡ [Rust SIMD] 4건 Batch 처리 완료!");
                        last_read_idx += 4;
                    } else {
                        break; // 경계선에 걸치면 아래의 1개씩 처리하는 로직으로 넘김
                    }
                }
            }

            // 🚀 남은 자투리 데이터(1~3개)나 경계선에 걸친 데이터 처리 (Fallback)
            while last_read_idx < current_java_tail {
                let target_idx = last_read_idx % 1024;
                let data_offset = (target_idx * 8) as usize;
                let val = u64::from_le_bytes((&mmap[data_offset..data_offset + 8]).try_into().unwrap());

                total_count += 1;

                if val >= 50000 {
                    alert_count += 1;
                    println!("🚨 [ALERT #{}] High Value: {} (Total: {})", alert_count, val, total_count);
                    fb_mmap[0..8].copy_from_slice(&alert_count.to_le_bytes());
                    let timestamp = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_millis();
                    let log_json = format!(r#"{{"timestamp": {}, "alert_id": {}, "value": {}}}"#, timestamp, alert_count, val);
                    writeln!(log_file, "{}", log_json).unwrap();
                } else {
                    println!("✅ [Normal] Value: {} (Total: {})", val, total_count);
                }
                last_read_idx += 1;
            }
        }

        thread::sleep(Duration::from_millis(1));
    }
}