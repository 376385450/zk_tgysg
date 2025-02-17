alter table tg_user_data_assets
    add ftp_path varchar(1024) null comment 'ftp路径';

alter table tg_user_data_assets
    add ftp_status varchar(16) null comment 'ftp上传状态: UPLOADING,FAILURE,SUCCESS';

alter table tg_user_data_assets
    add ftp_error_message text null comment 'ftp失败原因';

alter table tg_user_data_assets_snapshot
    add ftp_path varchar(1024) null comment 'ftp路径';

alter table tg_user_data_assets_snapshot
    add ftp_status varchar(16) null comment 'ftp上传状态: UPLOADING,FAILURE,SUCCESS';

alter table tg_user_data_assets_snapshot
    add ftp_error_message text null comment 'ftp失败原因';

alter table tg_project
    add customer_id bigint null comment '客户id' after description;

alter table tg_project
    add project_type int null comment '项目类型' after customer_id;

alter table tg_project
    add contract_number varchar(255) null after project_type;

alter table tg_project
    add project_manager bigint null after contract_number;

alter table tg_application_task_config modify column ` project_background ` text DEFAULT NULL COMMENT '项目背景描述';
alter table tg_application_task_config_snapshot modify column ` project_background ` text DEFAULT NULL COMMENT '项目背景描述';

alter table tg_project modify column ` description ` text DEFAULT NULL COMMENT '说明';


CREATE TABLE ` project_helper `
(
    `
    id
    `
    bigint
    NOT
    NULL
    AUTO_INCREMENT,
    `
    project_id
    `
    bigint
    NOT
    NULL,
    `
    user_id
    `
    bigint
    NOT
    NULL,
    PRIMARY
    KEY
(
    `
    id
    `
),
    UNIQUE KEY ` idx_biz `
(
    `
    project_id
    `,
    `
    user_id
    `
),
    KEY ` idx_user `
(
    `
    user_id
    `
)
    ) ENGINE=InnoDB COMMENT ='项目协作者';

CREATE TABLE ` tg_project_data_assets_relate `
(
    `
    id
    `
    int
    NOT
    NULL
    AUTO_INCREMENT,
    `
    project_id
    `
    bigint
    NOT
    NULL,
    `
    user_asset_id
    `
    bigint
    NOT
    NULL,
    `
    pro_type
    `
    varchar
(
    15
) DEFAULT NULL,
    PRIMARY KEY
(
    `
    id
    `
),
    UNIQUE KEY ` u_idx_apply_assets `
(
    `
    project_id
    `,
    `
    user_asset_id
    `
)
    )
insert ignore into tg_project_data_assets_relate(project_id, user_asset_id, pro_type)
select project_id, id, 'master'
from tg_user_data_assets
where project_id is not null
  and copy_from_id is not null;

insert ignore into tg_project_data_assets_relate(project_id, user_asset_id, pro_type)
select project_id, assets_id, 'master'
from tg_application_info
where project_id is not null
  and assets_id is not null;


CREATE TABLE ` t_customer `
(
    `
    id
    `
    int
    NOT
    NULL
    AUTO_INCREMENT,
    `
    short_name
    `
    varchar
(
    255
) DEFAULT NULL,
    ` full_name ` varchar
(
    255
) DEFAULT NULL,
    ` customer_type ` int DEFAULT NULL,
    ` customer_status ` int DEFAULT NULL COMMENT '0 禁用 1 启用',
    ` creator ` bigint DEFAULT NULL,
    ` create_time ` datetime DEFAULT NULL,
    ` updater ` bigint DEFAULT NULL,
    ` update_time ` datetime DEFAULT NULL,
    ` deleted ` bigint NOT NULL DEFAULT '0',
    PRIMARY KEY
(
    `
    id
    `
)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_0900_ai_ci COMMENT ='客户管理表';

alter table tg_application_info
    add application_no varchar(255) null;



alter table tg_application_info
    add column apply_desc varchar(200) null;

alter table arkbi_analysis
    add name varchar(255) null;
update data_dir d left join arkbi_analysis ap
on d.node_id = ap.id set ap.name = d.dir_name
where d.icon in ('chart', 'dashboard');
alter table tg_user_file_assets
    add project_id bigint not null;

truncate table tg_user_file_assets;

insert into project_helper(project_id, user_id)
select project_id, creator
from tg_user_data_assets
where project_id is not null
group by project_id, creator;


INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (197, '阿斯利康', '阿斯利康制药有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (198, '艾美', '艾美疫苗股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (199, '安斯泰来', '安斯泰来制药（中国）有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (200, '百济神州', '百济神州（北京）生物科技有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (201, '百特', '百特（中国）投资有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (202, '拜耳', '拜耳医药保健有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (203, '北京神州', '北京神州细胞生物技术集团股份公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00',
        0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (204, '北京同仁堂', '中国北京同仁堂（集团）有限责任公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00',
        0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (205, '贝达', '贝达药业股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (206, '倍特', '成都倍特药业股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (207, '勃林格', '勃林格殷格翰（中国）投资有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (208, '步长', '山东步长制药股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (209, '传奇', '传奇生物技术公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (210, '东阿', '东阿阿胶股份有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (211, '东宝', '通化东宝药业股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (212, '恩华', '江苏恩华药业股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (213, '费森尤斯卡比', '北京费森尤斯卡比医药有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00',
        0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (214, '甘李', '甘李药业股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (215, '高新', '长春高新技术产业（集团）股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (216, '广州医药', '广州医药集团有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (217, '海普瑞', '深圳市海普瑞药业集团股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (218, '海思科', '海思科医药集团股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (219, '海正', '浙江海正药业股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (220, '豪森', '江苏豪森药业集团有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (221, '和记', '和记黄埔医药（香港）投资有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (222, '赫力昂', '赫力昂', 2, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (223, '恒瑞', '江苏恒瑞医药股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (224, '红日', '天津红日药业股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (225, '华北制药', '华北制药集团有限责任公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (226, '华东医药', '华东医药股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (227, '华海', '浙江华海药业股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (228, '华兰', '华兰生物工程股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (229, '华润三九', '华润三九医药股份有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (230, '华润双鹤', '华润双鹤药业股份有限公司', 2, 1, 1, '2023-08-15 00:00:00', 1, '2023-08-15 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (231, '晖致', '晖致医药有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (232, '辉瑞', '辉瑞中国', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (233, '济川药业', '济川药业集团有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (234, '济民', '江西济民可信集团有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (235, '健康元', '健康元药业集团股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (236, '健友', '南京健友生化制药股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (237, '江中', '江中药业股份有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (238, '君实', '上海君实生物医药科技股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (239, '康恩贝', '浙江康恩贝制药股份有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (240, '康方', '中山康方生物医药有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (241, '康弘', '成都康弘药业集团股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (242, '康泰', '深圳康泰生物制品股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (243, '康希诺', '康希诺生物股份公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (244, '康缘', '江苏康缘药业股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (245, '康哲', '康哲药业控股有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (246, '科伦', '四川科伦药业股份有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (247, '科兴', '科兴控股生物技术有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (248, '葵花', '葵花药业集团股份有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (249, '昆药', '昆药集团股份有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (250, '莱士', '上海莱士血液制品股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (251, '礼来', '礼来苏州制药有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (252, '丽珠', '丽珠医药集团股份有限公司', 2, 1, 1, '2023-08-15 00:00:00', 1, '2023-08-15 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (253, '联邦', '联邦制药国际控股有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (254, '鲁南', '鲁南制药集团股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (255, '罗氏', '上海罗氏制药有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (256, '绿叶', '绿叶制药集团有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (257, '默克', '默克雪兰诺有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (258, '默沙东', '默沙东（中国）投资有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (259, '诺和诺德', '诺和诺德（中国）制药有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (260, '诺华', '北京诺华制药有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (261, '欧加隆', '欧加隆（上海）医药科技有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (262, '片仔癀', '漳州片仔癀药业股份有限公司', 2, 1, 1, '2023-10-30 00:00:00', 1, '2023-10-30 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (263, '普洛', '普洛药业股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (264, '齐鲁', '齐鲁制药集团有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (265, '强生', '强生（中国）投资有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (266, '人福', '人福医药集团股份公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (267, '赛诺菲', '赛诺菲（中国）投资有限公司', 2, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (268, '三生', '三生制药集团', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (269, '上海复星', '上海复星医药（集团）股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (270, '上海医药', '上海医药集团股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (271, '石家庄四药', '石家庄四药有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (272, '石药', '石药控股集团有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (273, '泰邦', '山东泰邦生物制品有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (274, '天津市医药', '天津市医药集团有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (275, '天士力', '天士力医药集团股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (276, '天坛', '北京天坛生物制品股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (277, '同辐', '中国同辐股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (278, '万泰', '北京万泰生物药业股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (279, '卫材', '卫材（中国）药业有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (280, '沃森', '云南沃森生物技术股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (281, '武田', '天津武田药品有限公司', 2, 1, 1, '2023-10-13 00:00:00', 1, '2023-10-13 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (282, '先声', '先声药业集团有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (283, '新和成', '新和成控股集团有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (284, '新华', '新华医药集团', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (285, '信达', '信达生物制药（苏州）有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (286, '信立泰', '深圳信立泰药业股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (287, '扬子江', '扬子江药业集团有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (288, '以岭', '石家庄以岭药业股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (289, '亿帆', '亿帆医药股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (290, '远大', '中国远大集团有限责任公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (291, '云南白药', '云南白药集团股份有限公司', 2, 1, 1, '2023-08-15 00:00:00', 1, '2023-08-15 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (292, '再鼎', '再鼎医药（上海）有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (293, '长江药业', '宜昌东阳光长江药业股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (294, '智飞', '重庆智飞生物制品股份有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (295, '中国生物', '中国生物制药有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (296, '中国医药', '中国医药集团有限公司', 2, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (297, 'GSK', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (298, 'Haleon', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (299, 'IMC', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (300, 'Manovia', null, 3, 1, 1, '2023-09-15 00:00:00', 1, '2023-09-15 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (301, 'OTC协会', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (302, 'ZS咨询', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (303, '艾伯维', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (304, '安徽汇达', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (305, '澳诺', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (306, '百洋', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (307, '北京法伯', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (308, '北京杏林', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (309, '北陆', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (310, '贝恩', null, 3, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (311, '贝泰妮', null, 3, 1, 1, '2023-07-12 00:00:00', 1, '2023-07-12 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (312, '博士伦', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (313, '参天', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (314, '常州制药', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (315, '诚意药业', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (316, '达仁堂', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (317, '达因', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (318, '第一三共', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (319, '多准', null, 3, 1, 1, '2023-07-12 00:00:00', 1, '2023-07-12 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (320, '佛灵', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (321, '福建新永惠', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (322, '古惠丰', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (323, '广峰', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (324, '广生堂', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (325, '广誉远', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (326, '贵阳新天', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (327, '百灵', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (328, '桂龙', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (329, '国风', null, 3, 1, 1, '2023-11-10 00:00:00', 1, '2023-11-10 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (330, '哈药', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (331, '海露', null, 3, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (332, '海默尼', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (333, '海森生物', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (334, '杭州民生', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (335, '杭州远大', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (336, '合肥大药房', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (337, '宏济堂', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (338, '葫芦娃药业', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (339, '华邦', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (340, '华创证券', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (341, '华领医药', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (342, '汇伦', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (343, '汇仁', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (344, '惠氏', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (345, '基立福', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (346, '吉利德', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (347, '吉瑞', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (348, '健合', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (349, '江苏豪森', null, 3, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (350, '江苏知原', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (351, '杰士邦', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (352, '杰特贝林', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (353, '界面新闻', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (354, '津一堂', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (355, '京东健康', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (356, '京都念慈庵', null, 3, 1, 1, '2023-10-13 00:00:00', 1, '2023-10-13 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (357, '久远谦长', null, 3, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (358, '可孚', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (359, '兰芝大药房', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (360, '郎迪', null, 3, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (361, '朗迪', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (362, '乐普', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (363, '李氏大药厂', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (364, '力生制药', null, 3, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (365, '利奥', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (366, '利洁时', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (367, '灵北', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (368, '罗盖全', null, 3, 1, 1, '2023-10-13 00:00:00', 1, '2023-10-13 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (369, '妈富隆', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (370, '马应龙', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (371, '曼伦', null, 3, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (372, '曼秀雷敦', null, 3, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (373, '美纳里尼', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (374, '民生', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (375, '南京同仁堂', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (376, '南京医药', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (377, '内蒙古惠丰国药', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (378, '诺诚健华', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (379, '欧姆龙', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (380, '奇正', null, 3, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (381, '雀巢', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (382, '人民同泰', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (383, '日健中外', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (384, '瑞霖', null, 3, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (385, '森世海亚', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (386, '山德士', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (387, '山东立健', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (388, '上药', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (389, '深圳瑞霖', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (390, '深圳中联', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (391, '神威药业', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (392, '施贵宝', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (393, '施维雅', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (394, '双鹤', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (395, '双鲸', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (396, '泰德', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (397, '泰恩康', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (398, '汤臣倍健', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (399, '天生', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (400, '万邦', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (401, '梧州制药', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (402, '仙乐健康', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (403, '香丹清', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (404, '向辉药业', null, 3, 1, 1, '2023-11-10 00:00:00', 1, '2023-11-10 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (405, '杏林', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (406, '幸福科达琳', null, 3, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (407, '雅培', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (408, '杨森', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (409, '养生堂', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (410, '宜草堂', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (411, '亿腾', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (412, '益普生', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (413, '意略明', null, 3, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (414, '英诺珐', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (415, '优比时', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (416, '优时比', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (417, '优思明', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (418, '鱼跃', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (419, '圆心', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (420, '再鼎医药', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (421, '赞邦', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (422, '张江', null, 3, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (423, '浙江医药', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (424, '振东', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (425, '正大丰海', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (426, '正大清江', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (427, '正大天晴', null, 3, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (428, '中美华东', null, 3, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (429, '中美史克', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (430, '中正大药房', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (431, '众生药业', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (432, '重庆登康', null, 3, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (433, '梓潼宫', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (434, '紫竹', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (435, '仙乐', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (436, 'CSL', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (437, '华领', null, 3, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (438, '康臣药业', '广东康臣药业集团', 3, 1, 1, '2023-12-18 00:00:00', 1, '2023-12-18 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (439, '百姓堂', null, 4, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (440, '阜新大德堂', null, 4, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (441, '高济', null, 4, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (442, '健一生', null, 4, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (443, '贵州一品', null, 4, 1, 1, '2023-07-12 00:00:00', 1, '2023-07-12 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (444, '国药控股', null, 4, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (445, '海王', null, 4, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (446, '湖南千金', null, 4, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (447, '老百姓', null, 4, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (448, '零售商', null, 4, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (449, '齐齐哈尔安泰大药房', null, 4, 1, 1, '2023-07-12 00:00:00', 1, '2023-07-12 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (450, '青岛医保城', null, 4, 1, 1, '2023-10-23 00:00:00', 1, '2023-10-23 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (451, '山西荣华', null, 4, 1, 1, '2023-07-12 00:00:00', 1, '2023-07-12 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (452, '陕西广济堂', null, 4, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (453, '徐州华医堂', null, 4, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (454, '一心堂', null, 4, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (455, '益丰', null, 4, 1, 1, '2023-10-13 00:00:00', 1, '2023-10-13 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (456, '云南怀德仁', null, 4, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (457, '云南普洱松茂', null, 4, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (458, '重庆平和', null, 4, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (459, '汇丰国药', null, 4, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (460, '健之佳', null, 4, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (461, '全亿', null, 4, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (462, '衡水百草堂', null, 4, 1, 1, '2023-11-17 00:00:00', 1, '2023-11-17 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (463, '重庆鑫斛', null, 4, 1, 1, '2023-11-17 00:00:00', 1, '2023-11-17 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (464, '山东幸福人', null, 4, 1, 1, '2023-11-17 00:00:00', 1, '2023-11-17 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (465, '柳州桂中', null, 4, 1, 1, '2023-11-17 00:00:00', 1, '2023-11-17 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (466, '康之佳', null, 4, 1, 1, '2023-12-18 00:00:00', 1, '2023-12-18 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (467, '北大', null, 5, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (468, '饿了么', null, 5, 1, 1, '2023-07-12 00:00:00', 1, '2023-07-12 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (469, '健康网', null, 5, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (470, '美团', null, 5, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (471, '铭格思咨询公司', null, 5, 1, 1, '2023-06-06 00:00:00', 1, '2023-06-06 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (472, '中信证券', null, 5, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (473, 'Power_BI', null, 6, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (474, '开思', null, 6, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (475, '瓴合', null, 6, 1, 1, '2023-10-20 00:00:00', 1, '2023-10-20 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (476, '生意营销罗盘', null, 6, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (477, '钛思', null, 6, 1, 1, '2023-10-20 00:00:00', 1, '2023-10-20 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (478, '中康', null, 6, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (479, '宜瓴通', null, 6, 1, 1, '2023-11-16 00:00:00', 1, '2023-11-16 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (480, '瓴西', null, 6, 1, 1, '2023-11-17 00:00:00', 1, '2023-11-17 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (481, '中药材', null, 6, 1, 1, '2023-11-17 00:00:00', 1, '2023-11-17 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (482, '瓴速', null, 6, 1, 1, '2023-11-17 00:00:00', 1, '2023-11-17 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (1074, '瓴通', null, 6, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (1075, '贵州健兴', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (1076, '武汉润禾', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (1077, '星创医药', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (1078, '贵州百灵', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (1079, '雷允上', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (1080, 'infoCN', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (1081, 'Swisse', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);
INSERT INTO t_customer (id, short_name, full_name, customer_type, customer_status, creator, create_time, updater,
                        update_time, deleted)
VALUES (1082, '凤翔传说', null, 3, 1, 1, '2023-01-01 00:00:00', 1, '2023-01-01 00:00:00', 0);


-- 客户数据
update tg_project
set customer_id = 478
where id = 1;
update tg_project
set customer_id = 478
where id = 2;
update tg_project
set customer_id = 279
where id = 3;
update tg_project
set customer_id = 298
where id = 8;
update tg_project
set customer_id = 298
where id = 14;
update tg_project
set customer_id = 478
where id = 18;
update tg_project
set customer_id = 1080
where id = 19;
update tg_project
set customer_id = 279
where id = 25;
update tg_project
set customer_id = 478
where id = 33;
update tg_project
set customer_id = 202
where id = 39;
update tg_project
set customer_id = 478
where id = 41;
update tg_project
set customer_id = 478
where id = 53;
update tg_project
set customer_id = 248
where id = 66;
update tg_project
set customer_id = 231
where id = 90;


-- 项目客户数据处理
update tg_application_info
set client_names = '拜耳'
where client_names = '北京拜耳';

update sys_user
set status = 0
where user_id = 43;

-- 项目经理
update tg_application_info
set pm = '时杰青'
where project_id = 1;
update tg_application_info
set pm = '时杰青'
where project_id = 2;
update tg_application_info
set pm = '吴怡彤'
where project_id = 3;
update tg_application_info
set pm = '李普红'
where project_id = 4;
update tg_application_info
set pm = '吴怡彤'
where project_id = 5;
update tg_application_info
set pm = '彭璐'
where project_id = 7;
update tg_application_info
set pm = '彭璐'
where project_id = 7;
update tg_application_info
set pm = '戴少萍'
where project_id = 8;
update tg_application_info
set pm = '谢嘉倩'
where project_id = 9;
update tg_application_info
set pm = '曹诺'
where project_id = 10;
update tg_application_info
set pm = '梁燕埼'
where project_id = 11;
update tg_application_info
set pm = '蔡彦如'
where project_id = 12;
update tg_application_info
set pm = '欧沛琳'
where project_id = 13;
update tg_application_info
set pm = '梁燕埼'
where project_id = 14;
update tg_application_info
set pm = '时杰青'
where project_id = 16;
update tg_application_info
set pm = '胡乐思'
where project_id = 17;
update tg_application_info
set pm = '钟思倩'
where project_id = 18;
update tg_application_info
set pm = '戴少萍'
where project_id = 19;
update tg_application_info
set pm = '王婷婷'
where project_id = 20;
update tg_application_info
set pm = '梁燕埼'
where project_id = 21;
update tg_application_info
set pm = '时杰青'
where project_id = 23;
update tg_application_info
set pm = '梁莉莉'
where project_id = 24;
update tg_application_info
set pm = '吴怡彤'
where project_id = 25;
update tg_application_info
set pm = '梁结莹'
where project_id = 26;
update tg_application_info
set pm = '卢颖翀'
where project_id = 27;
update tg_application_info
set pm = '陈泳洁'
where project_id = 28;
update tg_application_info
set pm = '雷玉洁'
where project_id = 29;
update tg_application_info
set pm = '欧沛琳'
where project_id = 31;
update tg_application_info
set pm = '梁结莹'
where project_id = 33;
update tg_application_info
set pm = '雷玉洁'
where project_id = 38;
update tg_application_info
set pm = '陈泳洁'
where project_id = 41;
update tg_application_info
set pm = '雷抒雁'
where project_id = 42;
update tg_application_info
set pm = '谢嘉倩'
where project_id = 43;
update tg_application_info
set pm = '张静'
where project_id = 45;
update tg_application_info
set pm = '赵昕'
where project_id = 46;
update tg_application_info
set pm = '李普红'
where project_id = 48;
update tg_application_info
set pm = '骆芷珊'
where project_id = 49;
update tg_application_info
set pm = '张静'
where project_id = 50;
update tg_application_info
set pm = '梁结莹'
where project_id = 53;
update tg_application_info
set pm = '雷玉洁'
where project_id = 54;
update tg_application_info
set pm = '花思桦'
where project_id = 55;
update tg_application_info
set pm = '雷玉洁'
where project_id = 58;
update tg_application_info
set pm = '方海涛'
where project_id = 60;
update tg_application_info
set pm = '梁燕埼'
where project_id = 61;
update tg_application_info
set pm = '戴少萍'
where project_id = 63;
update tg_application_info
set pm = '方海涛'
where project_id = 64;
update tg_application_info
set pm = '张静'
where project_id = 65;
update tg_application_info
set pm = '张仕淋'
where project_id = 68;
update tg_application_info
set pm = '何家哲'
where project_id = 70;
update tg_application_info
set pm = '李晓清'
where project_id = 71;
update tg_application_info
set pm = '梁莉莉'
where project_id = 72;
update tg_application_info
set pm = '雷玉洁'
where project_id = 73;
update tg_application_info
set pm = '冯嘉欣'
where project_id = 76;
update tg_application_info
set pm = '陈映'
where project_id = 78;
update tg_application_info
set pm = '何家哲'
where project_id = 80;
update tg_application_info
set pm = '胡乐思'
where project_id = 84;
update tg_application_info
set pm = '雷玉洁'
where project_id = 88;
update tg_application_info
set pm = '戴少萍'
where project_id = 89;
update tg_application_info
set pm = '梁燕埼'
where project_id = 90;
update tg_application_info
set pm = '彭璐'
where project_id = 93;
update tg_application_info
set pm = '梁燕埼'
where project_id = 95;
update tg_application_info
set pm = '胡乐思'
where project_id = 96;
update tg_application_info
set pm = '戴少萍'
where project_id = 97;
update tg_application_info
set pm = '彭璐'
where project_id = 98;
update tg_application_info
set pm = '梁燕埼'
where project_id = 99;
update tg_application_info
set pm = '戴少萍'
where project_id = 100;
update tg_application_info
set pm = '何家哲'
where project_id = 101;
update tg_application_info
set pm = '戴少萍'
where project_id = 102;
update tg_application_info
set pm = '彭璐'
where project_id = 104;
update tg_application_info
set pm = '戴少萍'
where project_id = 107;
update tg_application_info
set pm = '胡乐思'
where project_id = 109;
update tg_application_info
set pm = '何家哲'
where project_id = 111;
update tg_application_info
set pm = '吴怡彤'
where project_id = 112;
update tg_application_info
set pm = '梁焕诗'
where project_id = 114;
update tg_application_info
set pm = '何家哲'
where project_id = 116;
update tg_application_info
set pm = '谢嘉倩'
where project_id = 118;
update tg_application_info
set pm = '欧沛琳'
where project_id = 119;
update tg_application_info
set pm = '雷玉洁'
where project_id = 120;
update tg_application_info
set pm = '花思桦'
where project_id = 121;
update tg_application_info
set pm = '胡乐思'
where project_id = 126;
update tg_application_info
set pm = '梁燕埼'
where project_id = 127;
update tg_application_info
set pm = '雷玉洁'
where project_id = 128;
update tg_application_info
set pm = '雷玉洁'
where project_id = 129;
update tg_application_info
set pm = '雷玉洁'
where project_id = 131;
update tg_application_info
set pm = '雷玉洁'
where project_id = 132;
update tg_application_info
set pm = '蔡彦如'
where project_id = 133;
update tg_application_info
set pm = '高丹宜'
where project_id = 136;
update tg_application_info
set pm = '戴少萍'
where project_id = 137;
update tg_application_info
set pm = '丘天羽'
where project_id = 140;
update tg_application_info
set pm = '胡帆'
where project_id = 142;
update tg_application_info
set pm = '欧沛琳'
where project_id = 143;
update tg_application_info
set pm = '夏天立'
where project_id = 144;
update tg_application_info
set pm = '夏天立'
where project_id = 145;
update tg_application_info
set pm = '夏天立'
where project_id = 146;
update tg_application_info
set pm = '张静'
where project_id = 147;
update tg_application_info
set pm = '朝克'
where project_id = 152;
update tg_application_info
set pm = '胡乐思'
where project_id = 153;
update tg_application_info
set pm = '高丹宜'
where project_id = 160;
update tg_application_info
set pm = '雷玉洁'
where project_id = 161;
update tg_application_info
set pm = '邓宝瑶'
where project_id = 165;
update tg_application_info
set pm = '欧沛琳'
where project_id = 167;
update tg_application_info
set pm = '骆芷珊'
where project_id = 168;
update tg_application_info
set pm = '欧沛琳'
where project_id = 169;
update tg_application_info
set pm = '陈嘉森'
where project_id = 172;
update tg_application_info
set pm = '彭璐'
where project_id = 173;
update tg_application_info
set pm = '陈嘉森'
where project_id = 174;
update tg_application_info
set pm = '戴少萍'
where project_id = 176;
