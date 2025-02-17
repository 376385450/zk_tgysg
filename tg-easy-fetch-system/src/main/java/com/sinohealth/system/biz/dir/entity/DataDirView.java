package com.sinohealth.system.biz.dir.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 视图：tg_data_dir_view
 *
 * 注意在华为云RDS上创建视图前，需要先修改环境变量
 * show VARIABLES  like '%collation%';
 * show VARIABLES  like '%character_set_client%';
 *
 * SET default_collation_for_utf8mb4 = 'utf8mb4_general_ci';
 * SET collation_connection = 'utf8mb4_general_ci';
 *
 * drop view tg_data_dir_view;
 *
 * create view tg_data_dir_view AS
 * select v.*, d.parent_id, d.dir_name, d.sort
 * from (select id,
 *              create_user_id as owner_id,
 *              table_alias    as name,
 *              comment,
 *              create_time,
 *              'table'        as icon,
 *              dir_id,
 *              leader_name,
 *              0              as process_id,
 *              table_name,
 *              status
 *       from table_info
 *       where is_diy = 0
 *       union
 *       select id,
 *              owner_id,
 *              name,
 *              comment,
 *              create_time,
 *              'doc' as icon,
 *              dir_id,
 *              ''    as leader_name,
 *              process_id,
 *              ''    as table_name,
 *              status
 *       from tg_doc_info
 *       order by create_time desc) v
 *          left join data_dir d on d.id = v.dir_id;
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-02-21 10:57
 */
@Data
public class DataDirView {
    private Long id;
    private Long dirId;
    private Long parentId;
    private Long ownerId;
    private String name;
    private String comment;
    private String icon;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private String leaderName;
    private Long processId;
    private String tableName;
    private String dirName;
    @ApiModelProperty("模板类型")
    private String templateType;

    /**
     * 业务分类值 父级目录拼接
     */
    private String bizDirType;

    /**
     * 资产地图展示排序
     */
    private Integer disSort;
}
