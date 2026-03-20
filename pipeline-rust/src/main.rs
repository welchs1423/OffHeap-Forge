use memmap2::MmapOptions;
use std::fs::OpenOptions;
use std::thread;
use std::time::Duration;

fn main() -> Result<(), Box<dyn std::error::Error>> {
    println!("🦀 [Rust Analyzer] Real-time Filtering Online!");

    let file = OpenOptions::new().read(true).open("../forge-data.dat")?;
    let mmap = unsafe { MmapOptions::new().map(&file)? };

    let cursor_offset = 1024 * 8;
    let mut last_read_idx: u64 = 0;

    let mut total_count = 0;
    let mut alert_count = 0;

    loop {
        let tail_data = &mmap[cursor_offset..cursor_offset + 8];
        let current_java_tail = u64::from_le_bytes(tail_data.try_into()?);

        if current_java_tail > last_read_idx {
            let target_idx = (current_java_tail - 1) % 1024;
            let data_offset = (target_idx * 8) as usize;
            let val = u64::from_le_bytes((&mmap[data_offset..data_offset + 8]).try_into()?);

            total_count += 1; // 이제 이 값을 사용합니다!

            if val >= 50000 {
                alert_count += 1;
                println!("🚨 [ALERT #{}] High Value: {} (Total processed: {})", alert_count, val, total_count);
            } else {
                println!("✅ [Normal] Value: {} (Total processed: {})", val, total_count);
            }

            last_read_idx = current_java_tail;
        }

        thread::sleep(Duration::from_millis(1));
    }
}