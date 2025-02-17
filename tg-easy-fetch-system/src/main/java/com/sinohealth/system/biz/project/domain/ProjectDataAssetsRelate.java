package com.sinohealth.system.biz.project.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sinohealth.system.biz.project.constants.ProjectRelateEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author shallwetalk
 * @Date 2024/1/10
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("tg_project_data_assets_relate")
public class ProjectDataAssetsRelate {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 审核未通过时 该字段没值
     */
    private Long userAssetId;

    /**
     * 项目类型
     * @see ProjectRelateEnum
     */
    private String proType;

}



