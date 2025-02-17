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
 * 2024-05-25 09:33
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_assets_qc_batch_detail")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AssetsQcDetail implements AssetsVersion {

    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("业务关联id")
    private Long bizId;

    private Long batchId;

    private Long applicationId;

    private Long assetsId;

    private Long templateId;

    private Integer assetsVer;

    /**
     * 表
     */
    private String tableName;

    /**
     * @see AssetsUpgradeStateEnum
     */
    private String state;

    private LocalDateTime startTime;
    private LocalDateTime finishTime;

    /**
     * @see AssetsQcTypeEnum
     */
    private String assetsQcType;

    private String runLog;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @Override
    public Integer getVersion() {
        return assetsVer;
    }

}
