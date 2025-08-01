import random
import threading
import time
from datetime import datetime
from kafka import KafkaProducer
from faker import Faker

fake = Faker('zh_CN')

KAFKA_BROKER = '10.249.46.48:9092'
KAFKA_TOPIC = 'lingyu-log'

producer = KafkaProducer(
    bootstrap_servers=KAFKA_BROKER,
    value_serializer=lambda v: v.encode('utf-8'),
    linger_ms=5,
    batch_size=32768
)

# 随机生成日志内容函数

def generate_web_act():
    fields = [
        "web_act",
        datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
        str(random.randint(13000000000, 18000000000)),
        f"4600{random.randint(10**11, 10**12 - 1)}",
        "", "252050314",  # IMEI, ADSL账号
        random.choice(["西藏流亡国会", "世界维吾尔代表大会", "TG频道", "YouTube", "Twitter", "大纪元", "新唐人"]),
        random.choice(["tibetanparliament.org", "uyghurcongress.org", "t.me/vpninfo", "youtube.com", "twitter.com", "epochtimes.com", "ntdtv.com"]),
        random.choice(["涉藏", "涉恐", "涉证", "邪教", "非法集会", "反动"]),
        fake.ipv4_private(),
        fake.ipv4_public(),
        random.choice(["中国 上海", "中国 杭州", "中国 广州", "中国 深圳", "中国 北京"]),
        random.choice(["冰岛", "美国", "日本", "香港", "新加坡", "德国"]),
        random.choice(["SS", "Vmess", "Trojian", "Ipsec", "Openvpn", "WireGuard"]),
        random.choice(["移动", "联通", "电信", "广电"]),
        random.choice(["极速VPN", "蓝灯", "V2Ray", "GreenVPN", "天行VPN"]),
        str(random.randint(1024, 65535)),
        str(random.choice([443, 1080, 8443, 5986])),
        str(random.randint(1000, 50000)),
        str(random.randint(1000, 50000)),
        random.choice(["高", "中", "低"])
    ]
    return ",".join(fields)

def generate_tw_act():
    fields = [
        "tw_act",
        datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
        f"{int(time.time() * 1000)}_{random.randint(100, 999)}",
        str(random.randint(13000000000, 18000000000)),
        f"4600{random.randint(10**11, 10**12 - 1)}",
        "", "252050314",
        fake.ipv4_private(),
        fake.ipv4_public(),
        random.choice(["中国 上海", "中国 杭州", "中国 广州"]),
        random.choice(["意大利", "俄罗斯", "德国", "新加坡", "美国"]),
        random.choice(["SS", "Vmess", "Trojian", "Openvpn", "WireGuard"]),
        random.choice(["联通", "移动", "电信", "广电"]),
        random.choice(["绿叶VPN", "蓝灯", "V2Ray", "GreenVPN"]),
        str(random.randint(1000, 65535)),
        str(random.choice([443, 1080, 8443, 5986])),
        str(random.randint(500, 50000)),
        str(random.randint(500, 50000))
    ]
    return ",".join(fields)

def generate_tw_act_off():
    fields = [
        "tw_act_off",
        f"{int(time.time() * 1000)}_{random.randint(100, 999)}",
        datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
        str(random.randint(1000, 100000))
    ]
    return ",".join(fields)

def generate_app_act():
    fields = [
        "app_act",
        datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
        str(random.randint(13000000000, 18000000000)),
        f"4600{random.randint(10**11, 10**12 - 1)}",
        "", "252050314",
        fake.ipv4_private(),
        fake.ipv4_public(),
        random.choice(["Twitter", "YouTube", "Telegram", "Instagram", "Facebook", "TikTok", "WhatsApp"])
    ]
    return ",".join(fields)

def generate_declassify_act():
    fields = [
        "declassify_act",
        datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
        f"{int(time.time() * 1000)}_{random.randint(100, 999)}",
        str(random.randint(13000000000, 18000000000)),
        f"4600{random.randint(10**11, 10**12 - 1)}",
        "", "252050314",
        fake.ipv4_private(),
        str(random.randint(1024, 65535)),
        fake.ipv4_public(),
        str(random.choice([443, 1080, 5986])),
        random.choice(["中国 上海", "中国 杭州", "中国 深圳"]),
        random.choice(["法国", "美国", "荷兰", "英国"]),
        f"{fake.sha1()[:20]}.txt",
        "0",
        random.choice(["L2TP", "IPSec", "SS", "WireGuard"]),
        random.choice(["tftp", "https", "tls", "socks5"]),
        random.choice(["Instagram", "Twitter", "VPNApp", "翻墙助手"]),
        fake.domain_name()
    ]
    return ",".join(fields)

# 日志类型对应生成函数
generators = {
    "web_act":  generate_web_act,
    "tw_act": generate_tw_act,
    "tw_act_off": generate_tw_act_off,
    "app_act": generate_app_act,
    "declassify_act": generate_declassify_act
}


# 批量生成日志
def generate_batch_logs(batch_size: int):
    batch = []
    for _ in range(batch_size):
        log_type = random.choice(list(generators.keys()))
        msg = generators[log_type]()
        batch.append(msg)
    return batch

# 发送线程任务
def send_logs(batch_size=1000, target_per_second=100000):
    thread_count = target_per_second // batch_size
    interval = 1.0  # 每秒钟发 total 条

    def sender():
        while True:
            batch = generate_batch_logs(batch_size)
            for msg in batch:
                print(msg)
                producer.send(KAFKA_TOPIC, msg)
            # 不等待 flush，会自动批处理提升性能
            # producer.flush()  # 可选：降低延迟但增加负载

    # 启动线程
    threads = []
    for _ in range(thread_count):
        t = threading.Thread(target=sender)
        t.daemon = True
        t.start()
        threads.append(t)

    print(f"🚀 正在以约 {target_per_second}/秒速度发送日志，共 {thread_count} 个线程，每批 {batch_size} 条")
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("⚠️ 终止中...")
    finally:
        producer.flush()
        producer.close()


if __name__ == "__main__":
    send_logs(batch_size=1, target_per_second=1)