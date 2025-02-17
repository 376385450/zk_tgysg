-- arkbi分析记录
create table arkbi_analysis
(
    id                 bigint auto_increment comment 'ID'
        primary key,
    application_id     tinytext     null comment '申请ID,仪表板可能为多值,逗号分隔',
    analysis_id        varchar(255) not null comment 'BI分析ID',
    edit_url           text         null comment '编辑链接',
    preview_url        text         null comment '预览链接',
    create_by          bigint       not null comment '创建人ID',
    create_time        datetime     not null comment '创建时间',
    update_by          bigint       null comment '更新人ID',
    update_time        datetime     null comment '更新时间',
    status             int          not null comment '状态,0:图表未保存，1:图表已保存',
    type               varchar(10)  null comment '类型,dashboard:仪表板,chart:图表',
    share_url          text         null comment 'bi分享链接',
    share_url_password varchar(255) null comment '分享链接密码',
    constraint analysis_id_uindex
        unique (analysis_id)
)
    comment '分析视图';







