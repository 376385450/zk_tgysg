alter table tg_table_info_snapshot_compare
    add plan_execute_time datetime null comment '预计开始执行时间' after create_time;

INSERT INTO tg_kv_dict (id, name, val, create_time, update_time) VALUES (4, 'snapshotDiff-10054', '120', '2024-09-03 10:55:28', '2024-09-03 10:55:29');
