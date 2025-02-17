package com.sinohealth.system.biz.dict.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 同项目内自定义列
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-07-18 19:38
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_project_custom_field_dict")
@Accessors(chain = true)
public class ProjectCustomFieldDict {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目id
     */
    private Long projectId;

    /**
     * 字段库id
     */
    private Long fieldDictId;

    /**
     * 申请id
     */
    private Long applicationId;

//    /**
//     * 模板是否开启了 自定义列 任意区域
//     * @see TemplateGranularityDto#enableRangeTemplate
//     */
//    private Boolean templateOpen;

    private Long creator;

    private String bizType;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
