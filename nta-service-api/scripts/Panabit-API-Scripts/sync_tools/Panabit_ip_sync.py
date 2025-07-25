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
import requests
import json
import time
import urllib3
from requests_toolbelt.multipart.encoder import MultipartEncoder
from utils.redis_utils import RedisUtils

urllib3.disable_warnings()


class PanabitAPI:
    def __init__(self, ip, username, password, redis_host=None, redis_port=6379, redis_db=0, redis_password=None):
        self.base_url = f"https://{ip}/api/panabit.cgi"
        self.username = username
        self.password = password
        self.token = self.login()
        self.redis = RedisUtils(redis_host, redis_port, redis_db, redis_password)

    def login(self):
        """登录获取token"""
        payload = {
            "api_action": "api_login",
            "username": self.username,
            "password": self.password
        }
        response = requests.post(self.base_url, data=payload, verify=False)
        result = response.content.decode('gbk')
        data = json.loads(result)
        if data.get("code") == 0:
            print("[+] 登录成功")
            return str(data.get("data"))  # 确保token转为字符串
        else:
            raise Exception(f"[-] 登录失败: {data.get('msg')}")

    def list_group_ips(self, group_id, page=1, limit=500):
        """列出群组IP"""
        payload = {
            "api_route": "object@iptable",
            "api_action": "list_tabip",
            "id": group_id,
            "page": page,
            "limit": limit,
            "api_token": self.token
        }
        response = requests.post(self.base_url, data=payload, verify=False)
        result = response.content.decode('gbk')
        data = json.loads(result)
        return [entry["ip"] for entry in data.get("data", {}).get("data", [])]

    def clear_group_ips(self, group_id):
        """清空群组IP"""
        payload = {
            "api_route": "object@iptable",
            "api_action": "clear_tabip",
            "id": group_id,
            "api_token": self.token
        }
        response = requests.post(self.base_url, data=payload, verify=False)
        result = response.content.decode('gbk')
        return json.loads(result)

    def add_ip_file_to_group(self, group_id, ip_file_path):
        """
        通过multipart表单上传文件批量添加IP
        :param ip_file_path: 每行一个IP的文本文件路径
        """
        try:
            with open(ip_file_path, 'rb') as f:
                mp_encoder = MultipartEncoder(
                    fields={
                        'file': ('ip_list.txt', f, 'text/plain'),
                        'api_route': 'object@iptable',
                        'api_action': 'add_tabip',
                        'id': str(group_id),
                        'api_token': str(self.token)
                    }
                )

                response = requests.post(
                    self.base_url,
                    data=mp_encoder,
                    verify=False,
                    headers={
                        'Content-Type': mp_encoder.content_type,
                        'Connection': 'close'
                    },
                    timeout=30
                )

                if response.status_code != 200:
                    raise Exception(f"HTTP状态码异常: {response.status_code}")

                result = response.content.decode('gbk')
                return json.loads(result)

        except Exception as e:
            print(f"[-] 文件上传失败: {str(e)}")
            raise

    def save_ips_to_redis(self, temp_file, redis_key='ioc:ips') -> bool:
        """
        从 temp_file 读取 IP 列表，写入 Redis 的 Set。
        :param temp_file: 本地 IP 列表文件路径
        :param redis_key: Redis Key
        :return: True 表示成功写入，False 表示失败
        """
        try:
            with open(temp_file, 'r', encoding='utf-8') as f:
                ip_list = [line.strip() for line in f if line.strip() and not line.startswith('#')]

            if not ip_list:
                print("⚠️ IP 文件为空，未写入 Redis")
                return False

            self.redis.delete(redis_key)
            self.redis.write_set(redis_key, ip_list)
            print(f"✅ 已将 {len(ip_list)} 条 IP 写入 Redis：{redis_key}")
            return True

        except Exception as e:
            print(f"❌ 写入 Redis 失败: {e}")
            return False

    def sync_from_malicious(self, group_id, malicious_url):
        """同步IP列表"""
        temp_file = "malicious_ips.tmp"
        try:
            # 1. 下载IP列表
            print("[*] 正在获取IP列表...")
            response = requests.get(malicious_url, timeout=15)
            response.raise_for_status()

            # 2. 处理并保存临时文件
            print("[*] 处理IP列表...")
            valid_ips = []
            for line in response.text.splitlines():
                line = line.strip()
                if line and not line.startswith("#"):
                    valid_ips.append(line)

            with open(temp_file, 'w', encoding='utf-8') as f:
                f.write("\n".join(valid_ips))

            ip_count = len(valid_ips)
            print(f"[+] 获取到 {ip_count} 个有效IP")

            # 3. 清空现有群组
            print("[*] 正在清空群组...")
            clear_res = self.clear_group_ips(group_id)
            if clear_res.get("code") != 0:
                raise Exception(f"清空失败: {clear_res.get('msg')}")
            print("[+] 群组已清空")

            # 4. 批量上传新IP
            print("[*] 开始上传IP列表...")
            start_time = time.time()
            upload_res = self.add_ip_file_to_group(group_id, temp_file)

            if upload_res.get("code") == 0:
                print(f"[√] 上传成功 ({time.time() - start_time:.2f}s)")
            else:
                raise Exception(f"上传失败: {upload_res.get('msg')}")

            # 5. 将IP列表保存到Redis
            print("[*] 正在保存IP列表到Redis...")
            start_time = time.time()
            upload_res = self.save_ips_to_redis(temp_file, redis_key="ioc:ips")
            if upload_res:
                print(f"[√] 保存成功 ({time.time() - start_time:.2f}s)")
            else:
                raise Exception(f"保存失败: {upload_res.get('msg')}")
            self.redis.close()

        except requests.exceptions.RequestException as e:
            print(f"[-] 网络请求异常: {str(e)}")
            raise
        except json.JSONDecodeError as e:
            print(f"[-] JSON解析失败: {str(e)}")
            raise
        except Exception as e:
            print(f"[-] 同步异常: {str(e)}")
            raise
        finally:
            if os.path.exists(temp_file):
                os.remove(temp_file)
                print("[*] 已清理临时文件")


if __name__ == "__main__":
    # 配置参数
    CONFIG = {
        "gateway_ip": "你的Panabit设备IP",  # 例如: "192.168.0.100"
        "username": "你的Panabit用户名",  # 例如: "admin"
        "password": "你的Panabit密码",  # 例如: "panabit123"
        "group_id": 你的IP群组ID,  # **重要！** 可在Panabit WEB UI查看，例如: 1
        "malicious_url": "包含IP列表的URL",
        # 例如: "https://raw.githubusercontent.com/hagezi/dns-blocklists/refs/heads/main/ips/tif.txt"
        "redis_host": "你的Redis主机IP",
        "redis_port": 6379,
        "redis_password": "你的Redis密码",
        "redis_db": 0,
    }

    # 执行同步
    print(f"🚀 开始同步任务 {time.strftime('%Y-%m-%d %H:%M:%S')}")
    try:
        api = PanabitAPI(
            CONFIG["gateway_ip"],
            CONFIG["username"],
            CONFIG["password"],
            CONFIG["redis_host"],
            CONFIG["redis_port"],
            CONFIG["redis_db"],
            CONFIG["redis_password"]
        )
        api.sync_from_malicious(
            CONFIG["group_id"],
            CONFIG["malicious_url"]
        )
        print("✅ 同步任务完成")
    except Exception as e:
        print(f"❌ 任务异常终止: {str(e)}")
    finally:
        print(f"⏱️ 任务结束 {time.strftime('%H:%M:%S')}")
