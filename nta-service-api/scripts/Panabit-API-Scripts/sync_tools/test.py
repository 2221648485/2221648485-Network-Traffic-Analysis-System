import os
import sys
import io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')
# 当前脚本目录
CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))

# Panabit-API-Scripts 目录
PARENT_DIR = os.path.abspath(os.path.join(CURRENT_DIR, ".."))

# 把 Panabit-API-Scripts 加到 sys.path
sys.path.insert(0, PARENT_DIR)

# 现在再导入
from utils.redis_utils import RedisUtils

redis_utils = RedisUtils()
ip_list = ["11111","22222","33333"]
redis_utils.write_set("ioc:ip", ip_list)
domain_list = ["44444","55555","66666"]
redis_utils.write_set("ioc:domain", domain_list)

redis_utils.close()