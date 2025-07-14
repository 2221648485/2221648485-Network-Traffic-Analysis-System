# Traffic-Analysis接口文档-V1.0

## 1. 日志相关接口

### 1.1 解析网络访问行为日志

#### 1.1.1 基本信息

> 请求路径: "/log/parse/WebAccess"
>
> 请求方式: POST
>

#### 1.1.2 请求参数:

参数格式: 文件

#### 1.1.3 响应数据

```
{
  "code": 1,
  "msg": null,
  "data": [
    {
      "time": "2023-08-10 09:28:02",
      "phoneNumber": "16346235678",
      "imsi": "411009621181055",
      "imei": "",
      "adslAccount": "",
      "siteName": "西藏流亡国会",
      "siteUrl": "tibetanparliament.org",
      "siteType": "涉藏",
      "clientIp": "192.168.153.84",
      "serverIp": "1.209.58.52",
      "clientRegion": "中国 上海",
      "serverRegion": "冰岛 ",
      "tunnelType": "SSL",
      "operator": "联通",
      "tool": "极速VPN",
      "clientPort": 3606,
      "serverPort": 5494,
      "upBytes": 6014,
      "downBytes": 5621,
      "credibility": "高"
    }
  ]
}
```

