package com.sinohealth.system.biz.table.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sinohealth.common.enums.dataassets.TablePushPlanStateEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 宽表下一个版本时 推送资产升级 的 执行计划
 *
 * @author kuangchengping@sinohealth.cn
 * 2024-05-21 14:52
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_table_push_assets_plan")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class TablePushAssetsPlan {

    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long tableId;

    /**
     * 计划生成的版本
     */
    private Integer nextVersion;

    /**
     * 计划对比旧版本
     */
    private Integer preVersion;

    /**
     * @see TablePushPlanStateEnum
     */
    private String state;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

}
