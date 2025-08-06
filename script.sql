create table if not exists declassify_log
(
    id                 int auto_increment comment '主键自增ID'
        primary key,
    time               datetime      null comment '时间',
    flow_id            varchar(64)   null comment '流ID',
    phone_number       varchar(32)   null comment '手机号',
    imsi               varchar(32)   null comment 'IMSI',
    imei               varchar(32)   null comment 'IMEI',
    adsl_account       varchar(64)   null comment 'ADSL账号',
    client_ip          varchar(45)   null comment '客户端IP',
    client_port        int           null comment '客户端端口',
    server_ip          varchar(45)   null comment '服务器IP',
    server_port        int           null comment '服务器端口',
    client_region      varchar(64)   null comment '客户端地区',
    server_region      varchar(64)   null comment '服务器地区',
    original_file_name varchar(128)  null comment '原始文件名',
    packet_index       int           null comment '包号',
    network_protocol   varchar(32)   null comment '网络协议',
    app_protocol       varchar(32)   null comment '应用层协议',
    app_info           varchar(128)  null comment '应用层信息',
    host_name          varchar(128)  null comment '主机名信息',
    uid                int default 1 null comment '批次号',
    constraint uniq_declassify_log
        unique (flow_id, time, client_ip, server_ip, client_port, server_port, packet_index)
)
    comment '解密日志表（declassify_act）';

create table if not exists flow_image
(
    id               bigint auto_increment comment '主键ID，自增唯一标识'
        primary key,
    flow_id          varchar(100)                       null comment '流ID，可用于去重或关联分析',
    adsl_account     varchar(32)                        null comment '学号',
    start_time       datetime                           null comment '画像开始时间（上线时间）',
    end_time         datetime                           null comment '画像结束时间（下线时间）',
    total_up_bytes   bigint                             null comment '总上行字节数',
    total_down_bytes bigint                             null comment '总下行字节数',
    total_bytes      bigint                             null comment '上下行字节数之和',
    site_names       text                               null comment '访问网站域名列表（以逗号分隔）',
    tools            text                               null comment '使用的工具列表（如Shadowsocks、V2Ray等，逗号分隔）',
    tunnel_type      varchar(50)                        null comment '隧道类型（如VPN、SSH、SS等）',
    create_time      datetime default CURRENT_TIMESTAMP null comment '记录插入时间'
)
    comment '用户画像表：基于流ID聚合分析用户网络行为数据';

create index flow_image_flow_id_index
    on flow_image (flow_id);

create table if not exists foreign_app_access_log
(
    id           bigint auto_increment comment '主键ID'
        primary key,
    time         datetime      null comment '访问时间',
    phone_number varchar(20)   null comment '手机号',
    imsi         varchar(32)   null comment '国际移动用户识别码（IMSI）',
    imei         varchar(32)   null comment '设备识别码（IMEI）',
    adsl_account varchar(64)   null comment 'ADSL账号',
    client_ip    varchar(45)   null comment '客户端IP地址',
    server_ip    varchar(45)   null comment '服务器IP地址',
    app_name     varchar(100)  null comment '境外App名称',
    uid          int default 1 null comment '导入批次标识',
    constraint uniq_foreign_app_log
        unique (time, phone_number, client_ip, server_ip, app_name)
)
    comment '境外App访问行为日志';

create table if not exists risk_result
(
    id           bigint auto_increment comment '自增主键，唯一标识一条记录'
        primary key,
    phone_number varchar(20)                        null comment '用户手机号',
    adsl_account varchar(32)                        null comment '（作为用户唯一标识）',
    risk_level   varchar(10)                        not null comment '风险等级（如：LOW/MEDIUM/HIGH）',
    window_start datetime                           not null comment '窗口起始时间（聚合计算的时间窗口开始点）',
    window_end   datetime                           not null comment '窗口结束时间（聚合计算的时间窗口结束点）',
    msg          text                               null comment '风险描述信息（如：频繁访问敏感IP）',
    create_time  datetime default CURRENT_TIMESTAMP null comment '记录创建时间（数据写入数据库的时间）',
    status       varchar(64)                        null comment '是否被查询过',
    constraint uk_phone_window
        unique (adsl_account, window_start, window_end) comment '防止同一用户同一窗口的重复数据'
)
    comment '用户风险评分结果表（按窗口聚合）';

create table if not exists tunnel_access_log
(
    id            bigint auto_increment comment '自增主键'
        primary key,
    time          datetime      not null comment '时间',
    flow_id       varchar(64)   null comment '流ID',
    phone_number  varchar(20)   null comment '手机号',
    imsi          varchar(32)   null comment 'IMSI',
    imei          varchar(32)   null comment 'IMEI',
    adsl_account  varchar(64)   null comment 'ADSL账号',
    client_ip     varchar(45)   null comment '客户端IP',
    server_ip     varchar(45)   null comment '服务器IP',
    client_region varchar(64)   null comment '客户端地区',
    server_region varchar(64)   null comment '服务器属地',
    tunnel_type   varchar(32)   null comment '隧道类型',
    operator      varchar(32)   null comment '运营商',
    tool          varchar(64)   null comment '工具',
    client_port   int           null comment '客户端口',
    server_port   int           null comment '服务器端口',
    up_bytes      bigint        null comment '上行包大小（Byte）',
    down_bytes    bigint        null comment '下行包大小（Byte）',
    uid           int default 1 null comment '解析批次标识',
    constraint uniq_tunnel_log
        unique (flow_id, time, client_ip, server_ip, client_port, server_port)
)
    comment '突网行为日志表';

create index idx_phone
    on tunnel_access_log (phone_number);

create index idx_time
    on tunnel_access_log (time);

create index idx_uid
    on tunnel_access_log (uid);

create table if not exists tunnel_offline_log
(
    id           int auto_increment comment '自增主键'
        primary key,
    flow_id      varchar(64)   not null comment '流ID',
    offline_time datetime      not null comment '下线时间',
    total_bytes  bigint        not null comment '流总大小（单位 Byte）',
    uid          int default 1 null comment '解析批次标识',
    constraint uniq_tunnel_offline
        unique (flow_id, offline_time)
)
    comment '突网行为下线日志表' collate = utf8mb4_unicode_ci;

create table if not exists unified_log
(
    id                 bigint auto_increment
        primary key,
    time               datetime                           null,
    type               varchar(50)                        null,
    phone_number       varchar(20)                        null,
    imsi               varchar(20)                        null,
    imei               varchar(20)                        null,
    adsl_account       varchar(50)                        null,
    client_ip          varchar(50)                        null,
    server_ip          varchar(50)                        null,
    client_region      varchar(100)                       null,
    server_region      varchar(100)                       null,
    tunnel_type        varchar(50)                        null,
    operator           varchar(50)                        null,
    tool               varchar(100)                       null,
    site_name          varchar(255)                       null,
    site_url           varchar(500)                       null,
    site_type          varchar(50)                        null,
    client_port        int                                null,
    server_port        int                                null,
    up_bytes           bigint                             null,
    down_bytes         bigint                             null,
    credibility        varchar(50)                        null,
    flow_id            varchar(100)                       null,
    packet_index       int                                null,
    original_file_name varchar(255)                       null,
    offline_time       datetime                           null,
    total_bytes        bigint                             null,
    app_name           varchar(255)                       null,
    network_protocol   varchar(100)                       null,
    app_protocol       varchar(100)                       null,
    app_info           varchar(255)                       null,
    host_name          varchar(255)                       null,
    uid                int                                null,
    raw_line           text                               null,
    create_time        datetime default CURRENT_TIMESTAMP null
);

create table if not exists web_access_log
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    time          datetime      null comment '时间',
    phone_number  varchar(20)   null comment '手机号',
    imsi          varchar(30)   null comment 'IMSI',
    imei          varchar(30)   null comment 'IMEI',
    adsl_account  varchar(50)   null comment 'adsl账号',
    site_name     varchar(255)  null comment '访问网站名称',
    site_url      varchar(255)  null comment '访问网站网址',
    site_type     varchar(50)   null comment '网站类型',
    client_ip     varchar(45)   null comment '客户端IP',
    server_ip     varchar(45)   null comment '服务器IP',
    client_region varchar(100)  null comment '客户端地区',
    server_region varchar(100)  null comment '服务器地区',
    tunnel_type   varchar(50)   null comment '隧道类型',
    operator      varchar(50)   null comment '运营商',
    tool          varchar(100)  null comment '工具',
    client_port   int           null comment '客户端口',
    server_port   int           null comment '服务器端口',
    up_bytes      bigint        null comment '上行包大小（单位Byte）',
    down_bytes    bigint        null comment '下行包大小（单位Byte）',
    credibility   varchar(20)   null comment '可信度',
    uid           int default 1 null comment '次序标识',
    constraint uniq_web_log
        unique (time, phone_number, client_ip, server_ip, client_port, server_port, site_url(100))
)
    comment '网站访问日志表';


