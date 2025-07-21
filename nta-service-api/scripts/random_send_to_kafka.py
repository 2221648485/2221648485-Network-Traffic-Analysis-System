import random
import time
from datetime import datetime
from kafka import KafkaProducer
from faker import Faker

fake = Faker('zh_CN')

# Kafka 配置
KAFKA_BROKER = 'localhost:9092'
KAFKA_TOPIC = 'test'  # 可根据日志类型拆分多个 topic

producer = KafkaProducer(
    bootstrap_servers=KAFKA_BROKER,
    value_serializer=lambda v: v.encode('utf-8')
)

# 随机生成日志内容函数

FIXED_PHONE_NUMBERS = [
    "13800138000", "13900139000", "13700137000", "13600136000", "13500135000",
    "13400134000", "13300133000", "13200132000", "13100131000", "13000130000",
    "15000150000", "15100151000", "15200152000", "15300153000", "15500155000",
    "15600156000", "15700157000", "15800158000", "15900159000", "18800188000"
]

def generate_web_act():
    fields = [
        "web_act",
        datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
        random.choice(FIXED_PHONE_NUMBERS),
        f"4600{random.randint(10**11, 10**12 - 1)}",
        "", "",  # IMEI, ADSL账号
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
        random.choice(FIXED_PHONE_NUMBERS),
        f"4600{random.randint(10**11, 10**12 - 1)}",
        "", "",
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
        random.choice(FIXED_PHONE_NUMBERS),
        f"4600{random.randint(10**11, 10**12 - 1)}",
        "", "",
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
        random.choice(FIXED_PHONE_NUMBERS),
        f"4600{random.randint(10**11, 10**12 - 1)}",
        "", "",
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

# 发送主循环
if __name__ == "__main__":
    try:
        while True:
            log_type = random.choice(list(generators.keys()))
            message = generators[log_type]()
            print(f"[{log_type}] {message}")
            producer.send(KAFKA_TOPIC, message)
            time.sleep(0.5)
    except KeyboardInterrupt:
        print("已终止发送。")
    finally:
        producer.close()
