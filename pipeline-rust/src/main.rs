use memmap2::MmapOptions;
use std::fs::OpenOptions;
use std::thread;
use std::time::Duration;

fn main() -> Result<(), Box<dyn std::error::Error>> {
    println!("🦀 [Rust Analyzer] Two-Way IPC Mode Online!");

    // 1. 자바가 주는 데이터 파일 (읽기 전용)
    let file = OpenOptions::new().read(true).open("../forge-data.dat")?;
    let mmap = unsafe { MmapOptions::new().map(&file)? };

    // 2. 🚀 [추가] 자바가 만든 우체통 파일 (읽기/쓰기 가능)
    let fb_file = OpenOptions::new().read(true).write(true).open("../forge-feedback.dat")?;
    let mut fb_mmap = unsafe { MmapOptions::new().map_mut(&fb_file)? };

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
                println!("🚨 [ALERT #{}] High Value: {} (Total processed: {})", alert_count, val, total_count);

                // 🚀 [추가] 우체통(fb_mmap)의 첫 8바이트에 현재 경고 횟수(alert_count)를 덮어씀!
                fb_mmap[0..8].copy_from_slice(&alert_count.to_le_bytes());
            } else {
                println!("✅ [Normal] Value: {} (Total processed: {})", val, total_count);
            }

            last_read_idx = current_java_tail;
        }

        thread::sleep(Duration::from_millis(1));
    }
}