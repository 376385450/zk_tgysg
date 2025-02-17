package com.sinohealth.system.biz.project.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sinohealth.common.core.domain.IdTable;
import com.sinohealth.common.enums.StatusTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-29 11:30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_project")
@Accessors(chain = true)
public class Project implements IdTable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    /**
     * 项目背景
     */
    private String description;

    private Long customerId;

    private Long projectManager;

    /**
     * @see StatusTypeEnum
     */
    private Integer status;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("更新人")
    private Long updater;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
