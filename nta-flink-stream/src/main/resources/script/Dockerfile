# 使用官方 Python 3.10 轻量版镜像
FROM python:3.10-slim

ENV PYTHONIOENCODING=utf-8

# 设置工作目录
WORKDIR /app

# 复制依赖文件
COPY requirements.txt ./

# 安装依赖
RUN pip install --no-cache-dir -r requirements.txt

# 复制Python脚本
COPY send_to_kafka.py ./

# 运行脚本
CMD ["python", "send_to_kafka.py"]

