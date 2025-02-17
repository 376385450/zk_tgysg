package com.sinohealth.system.biz.dataassets.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sinohealth.system.biz.dataassets.constant.AssetsQcTypeEnum;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 09:41
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_assets_qc_batch")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AssetsQcBatch {

    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("业务关联id")
    private Long bizId;

    private String batchNo;

    /**
     * 模板id 逗号拼接
     */
    private String templateId;

    /**
     * 模板名
     */
    private String templateName;

    /**
     * wait 创建批次 等其中一方执行完（sku/品牌）表在合并
     * running 工作流在执行
     *
     * @see AssetsUpgradeStateEnum
     */
    private String state;
    private String skuState;
    private String brandState;

    /**
     * 工作流实例 UUID
     */
    private String flowInstanceId;

    /**
     * @see AssetsQcTypeEnum
     */
    private String actType;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
