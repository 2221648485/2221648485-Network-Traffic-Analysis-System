from kafka import KafkaProducer

# Kafka配置
KAFKA_BROKER = '10.249.46.48:9092'  # 替换为你的 Kafka 地址
KAFKA_TOPIC = 'lingyu-log'  # 替换为你要发送的 topic 名称

# 创建生产者
producer = KafkaProducer(
    bootstrap_servers=KAFKA_BROKER,
    value_serializer=lambda v: v.encode('utf-8')  # 将字符串编码为 UTF-8 字节
)

# 发送单条消息
message = "tw_act_off,2025073100010000052003,2025-07-31 14:46:03,39547"
producer.send(KAFKA_TOPIC, value=message)

# 关闭生产者
producer.flush()
producer.close()

print("消息发送成功：", message)
