import paramiko
import os
import json
from kafka import KafkaProducer
from datetime import datetime, timedelta

# Kafka 配置
KAFKA_BROKER = 'localhost:9092'
KAFKA_TOPIC = 'lingyu_log'

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
    'tw_act': 'tw_act',
    'tw_act_off': 'tw_act_off',
    'app_act': 'app_act',
    'declassify_act': 'declassify_act'
}

# 加载记录文件
def load_processed_files():
    if os.path.exists(RECORD_FILE):
        with open(RECORD_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    return {}

# 保存记录文件
def save_processed_files(data):
    with open(RECORD_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

# 清除半个月前的记录
def clean_old_records(records):
    today = datetime.now().date()
    cutoff = today - timedelta(days=15)
    return {date: files for date, files in records.items() if datetime.strptime(date, '%Y-%m-%d').date() >= cutoff}

# 发送日志到 Kafka
def send_line_to_kafka(line, prefix, producer):
    full_line = f"{prefix},{line.strip()}"
    producer.send(KAFKA_TOPIC, value=full_line.encode('utf-8'))

# 下载 + 发送
def process_file(sftp, remote_path, prefix, producer):
    local_path = os.path.join(LOCAL_TEMP_DIR, os.path.basename(remote_path))
    sftp.get(remote_path, local_path)

    with open(local_path, 'r', encoding='utf-8') as f:
        for line in f:
            if line.strip():
                send_line_to_kafka(line, prefix, producer)

    os.remove(local_path)

# 判断是否是今天的文件（根据文件名）
def is_today_file(filename):
    today_str = datetime.now().strftime('%Y%m%d')
    return today_str in filename

def get_prefix_for_file(filename):
    for key in FILE_PREFIX_MAP:
        if key in filename:
            return FILE_PREFIX_MAP[key]
    return None

def main():
    producer = KafkaProducer(bootstrap_servers=KAFKA_BROKER)
    transport = paramiko.Transport((SFTP_HOST, SFTP_PORT))
    transport.connect(username=SFTP_USER, password=SFTP_PASSWORD)
    sftp = paramiko.SFTPClient.from_transport(transport)

    try:
        sftp.chdir(SFTP_REMOTE_DIR)
        files = sftp.listdir()

        processed_files = load_processed_files()
        today_str = datetime.now().strftime('%Y-%m-%d')
        processed_today = processed_files.get(today_str, [])

        for file in files:
            if not (file.endswith('.txt') and is_today_file(file)):
                continue
            if file in processed_today:
                continue

            prefix = get_prefix_for_file(file)
            if prefix:
                remote_path = os.path.join(SFTP_REMOTE_DIR, file)
                print(f"Processing {remote_path} with prefix {prefix}")
                process_file(sftp, remote_path, prefix, producer)
                processed_today.append(file)

        processed_files[today_str] = processed_today
        cleaned_records = clean_old_records(processed_files)
        save_processed_files(cleaned_records)

    finally:
        sftp.close()
        transport.close()
        producer.close()

if __name__ == '__main__':
    main()
