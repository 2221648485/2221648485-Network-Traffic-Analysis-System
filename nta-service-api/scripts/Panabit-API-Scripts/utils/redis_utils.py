

import redis
import logging
from typing import List

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class RedisUtils:
    def __init__(self, host='localhost', port=6379, db=0, password=None):
        try:
            self.pool = redis.ConnectionPool(
                host=host,
                port=port,
                db=db,
                password=password,
                decode_responses=True,  # 自动转为 str
                max_connections=10
            )
            self.client = redis.Redis(connection_pool=self.pool)
            # 测试连接
            self.client.ping()
            logger.info("✅ Redis connected successfully")
        except Exception as e:
            logger.error("❌ Redis connection failed: %s", e)
            raise

    def write_set(self, key: str, values: List[str]):
        """
        将 values 批量写入 Redis Set 中，先删除原 key
        """
        try:
            self.client.delete(key)
            if values:
                self.client.sadd(key, *values)
                logger.info(f"✅ 写入 Redis 成功：{key} 共 {len(values)} 条")
            else:
                logger.warning(f"⚠️ 空值列表，未写入：{key}")
        except Exception as e:
            logger.error(f"❌ 写入 Redis 失败：{key} - {e}")
            raise

    def get_set_members(self, key: str) -> List[str]:
        """
        获取 Set 中的所有值
        """
        try:
            members = list(self.client.smembers(key))
            logger.info(f"✅ 读取 Redis 成功：{key} 共 {len(members)} 条")
            return members
        except Exception as e:
            logger.error(f"❌ 读取 Redis 失败：{key} - {e}")
            return []

    def close(self):
        """
        显式释放连接
        """
        if self.pool:
            self.pool.disconnect()
            logger.info("🔌 Redis 连接池已关闭")

