import paramiko
import os
import json
from kafka import KafkaProducer
from datetime import datetime, timedelta
import schedule
import time

# Kafka 配置
KAFKA_BROKER = 'localhost:9092'
KAFKA_TOPIC = 'lingyu-log'

# SFTP 配置
SFTP_HOST = '192.168.112.109'
SFTP_PORT = 22
SFTP_USER = 'root'
SFTP_PASSWORD = 'BDSsusan22'
SFTP_REMOTE_DIR = '../data/'

# 本地临时目录 & 已处理文件记录
LOCAL_TEMP_DIR = './tmp_logs'
RECORD_FILE = './processed_files.json'
os.makedirs(LOCAL_TEMP_DIR, exist_ok=True)

# 日志前缀映射
FILE_PREFIX_MAP = {
    'web_act': 'web_act',
    'tw_act_off': 'tw_act_off',
    'tw_act': 'tw_act',
    'app_act': 'app_act',
    'declassify_act': 'declassify_act'
}


# 加载已处理文件记录
def load_processed_files():
    if os.path.exists(RECORD_FILE):
        with open(RECORD_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    return {}


# 保存已处理文件记录
def save_processed_files(data):
    with open(RECORD_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)


# 清理半个月前的记录
def clean_old_records(records):
    today = datetime.now().date()
    cutoff = today - timedelta(days=15)
    return {
        date: files for date, files in records.items()
        if datetime.strptime(date, '%Y-%m-%d').date() >= cutoff
    }


# 判断是否是最近 N 天的文件
def is_recent_file(filename, days=2):
    for i in range(days):
        date_str = (datetime.now() - timedelta(days=i)).strftime('%Y%m%d')
        if date_str in filename:
            return True
    return False


# 获取文件前缀
def get_prefix_for_file(filename):
    for key in FILE_PREFIX_MAP:
        if key in filename:
            return FILE_PREFIX_MAP[key]
    return None


# 发送日志到 Kafka
def send_line_to_kafka(line, prefix, producer):
    full_line = f"{prefix},{line.strip()}"
    # print(f"[发送] 正在发送日志：{full_line}")
    producer.send(KAFKA_TOPIC, value=full_line.encode('utf-8'))


# 下载并处理文件
def process_file(sftp, remote_path, prefix, producer):
    local_path = os.path.join(LOCAL_TEMP_DIR, os.path.basename(remote_path))
    sftp.get(remote_path, local_path)

    with open(local_path, 'r', encoding='utf-8') as f:
        for line in f:
            if line.strip():
                send_line_to_kafka(line, prefix, producer)

    os.remove(local_path)


# 主任务
def main():
    print(f"[信息] 开始执行任务时间：{datetime.now()}")
    producer = KafkaProducer(bootstrap_servers=KAFKA_BROKER)
    transport = paramiko.Transport((SFTP_HOST, SFTP_PORT))
    transport.connect(username=SFTP_USER, password=SFTP_PASSWORD)
    sftp = paramiko.SFTPClient.from_transport(transport)

    try:
        sftp.chdir(SFTP_REMOTE_DIR)
        files = sftp.listdir()

        # 加载所有历史已处理文件名
        processed_files = load_processed_files()
        all_processed = set(f for v in processed_files.values() for f in v)

        # 当前运行日期用于记录新处理的文件
        current_date = datetime.now().strftime('%Y-%m-%d')
        if current_date not in processed_files:
            processed_files[current_date] = []

        for file in files:
            if not (file.endswith('.txt') and is_recent_file(file)):
                continue
            if file in all_processed:
                continue

            prefix = get_prefix_for_file(file)
            if prefix:
                remote_path = os.path.join(SFTP_REMOTE_DIR, file)
                print(f"[处理] 正在处理文件：{remote_path}（前缀：{prefix}）")
                process_file(sftp, remote_path, prefix, producer)
                processed_files[current_date].append(file)
            else:
                print(f"[跳过] 未识别前缀的文件：{file}")

        cleaned_records = clean_old_records(processed_files)
        save_processed_files(cleaned_records)

        print(f"[完成] 本轮任务完成，共处理文件数：{len(processed_files[current_date])}")

    except Exception as e:
        print(f"[错误] 执行任务时发生异常：{e}")

    finally:
        sftp.close()
        transport.close()
        producer.close()
        print(f"[信息] 任务结束时间：{datetime.now()}")


# 定时任务封装
def job():
    try:
        print("\n==============================")
        print("[调度] 定时任务开始执行...")
        main()
        print("[调度] 定时任务执行完成。")
        print("==============================\n")
    except Exception as e:
        print(f"[异常] 定时任务失败：{e}")


# 启动调度器
if __name__ == '__main__':
    t = 1
    # 立即执行一次
    job()
    schedule.every(t).minutes.do(job)
    print(f"[启动] SFTP 到 Kafka 日志调度器已启动（每 {t} 分钟执行一次）...")

    while True:
        schedule.run_pending()
        time.sleep(1)
