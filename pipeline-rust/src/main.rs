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
            let target_idx = (current_java_tail - 1) % 1024;
            let data_offset = (target_idx * 8) as usize;
            let val = u64::from_le_bytes((&mmap[data_offset..data_offset + 8]).try_into()?);

            total_count += 1;

            if val >= 50000 {
                alert_count += 1;
                println!("🚨 [ALERT #{}] High Value: {} (Total: {})", alert_count, val, total_count);

                // 자바 우체통에 경고 횟수 업데이트
                fb_mmap[0..8].copy_from_slice(&alert_count.to_le_bytes());

                // 🚀 [Phase 43] JSON 형태로 로그 파일에 기록!
                let timestamp = SystemTime::now().duration_since(UNIX_EPOCH)?.as_millis();
                let log_json = format!(r#"{{"timestamp": {}, "alert_id": {}, "value": {}}}"#, timestamp, alert_count, val);
                writeln!(log_file, "{}", log_json)?;
            } else {
                println!("✅ [Normal] Value: {} (Total: {})", val, total_count);
            }

            last_read_idx = current_java_tail;
        }

        thread::sleep(Duration::from_millis(1));
    }
}