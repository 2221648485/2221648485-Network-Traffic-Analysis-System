import os
import json
from kafka import KafkaProducer
from datetime import datetime, timedelta
import pytz
import schedule
import time

# Kafka 配置
KAFKA_BROKER = 'localhost:9092'
KAFKA_TOPIC = 'lingyu-log'

# 本地日志目录（替代之前的SFTP目录）
LOCAL_LOG_DIR = '/data/'  # 这里改成你Linux服务器本地日志目录路径

# 已处理文件记录
RECORD_FILE = './processed_files.json'
os.makedirs(LOCAL_LOG_DIR, exist_ok=True)

# 日志前缀映射
FILE_PREFIX_MAP = {
    'web_act': 'web_act',
    'tw_act_off': 'tw_act_off',
    'tw_act': 'tw_act',
    'app_act': 'app_act',
    'declassify_act': 'declassify_act'
}

# 上海时区对象
tz = pytz.timezone('Asia/Shanghai')

def now():
    return datetime.now(tz)

def load_processed_files():
    if os.path.exists(RECORD_FILE):
        with open(RECORD_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    return {}

def save_processed_files(data):
    with open(RECORD_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

def clean_old_records(records):
    today = now().date()
    cutoff = today - timedelta(days=15)
    return {
        date: files for date, files in records.items()
        if datetime.strptime(date, '%Y-%m-%d').date() >= cutoff
    }

def is_recent_file(filename, days=2):
    for i in range(days):
        date_str = (now() - timedelta(days=i)).strftime('%Y%m%d')
        if date_str in filename:
            return True
    return False

def get_prefix_for_file(filename):
    for key in FILE_PREFIX_MAP:
        if key in filename:
            return FILE_PREFIX_MAP[key]
    return None

def send_line_to_kafka(line, prefix, producer):
    full_line = f"{prefix},{line.strip()}"
    producer.send(KAFKA_TOPIC, value=full_line.encode('utf-8'))

def process_file(local_path, prefix, producer):
    with open(local_path, 'r', encoding='utf-8') as f:
        for line in f:
            if line.strip():
                send_line_to_kafka(line, prefix, producer)

def main():
    print(f"[信息] 开始执行任务时间：{now()}")
    producer = KafkaProducer(bootstrap_servers=KAFKA_BROKER)

    try:
        files = os.listdir(LOCAL_LOG_DIR)

        processed_files = load_processed_files()
        all_processed = set(f for v in processed_files.values() for f in v)

        current_date = now().strftime('%Y-%m-%d')
        if current_date not in processed_files:
            processed_files[current_date] = []

        for file in files:
            if not (file.endswith('.txt') and is_recent_file(file)):
                continue
            if file in all_processed:
                continue

            prefix = get_prefix_for_file(file)
            if prefix:
                local_path = os.path.join(LOCAL_LOG_DIR, file)
                print(f"[处理] 正在处理文件：{local_path}（前缀：{prefix}）")
                process_file(local_path, prefix, producer)
                processed_files[current_date].append(file)
            else:
                print(f"[跳过] 未识别前缀的文件：{file}")

        cleaned_records = clean_old_records(processed_files)
        save_processed_files(cleaned_records)

        print(f"[完成] 本轮任务完成，共处理文件数：{len(processed_files[current_date])}")

    except Exception as e:
        print(f"[错误] 执行任务时发生异常：{e}")

    finally:
        producer.close()
        print(f"[信息] 任务结束时间：{now()}")

def job():
    try:
        print("\n==============================")
        print("[调度] 定时任务开始执行...")
        main()
        print("[调度] 定时任务执行完成。")
        print("==============================\n")
    except Exception as e:
        print(f"[异常] 定时任务失败：{e}")

if __name__ == '__main__':
    t = 10
    job()
    schedule.every(t).minutes.do(job)
    print(f"[启动] 本地日志到 Kafka 调度器已启动（每 {t} 分钟执行一次）...")

    while True:
        schedule.run_pending()
        time.sleep(1)
