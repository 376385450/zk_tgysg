package com.sinohealth.system.biz.dataassets.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.common.enums.dataassets.AssetsCompareTypeEnum;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsCompareInvokeRequest;
import com.sinohealth.system.biz.dataassets.helper.AssetsCompareInvoker;
import com.sinohealth.system.biz.dataassets.service.impl.AssetsCompareServiceImpl;
import com.sinohealth.system.biz.dataassets.service.impl.AssetsUpgradeTriggerServiceImpl;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-16 15:03
 * @see AssetsUpgradeTriggerServiceImpl#createAssetsCompare 底表触发批量创建
 * @see AssetsCompareServiceImpl#createCompare 手动创建
 * @see AssetsCompareInvoker#invokeCompareReq(AssetsCompareInvokeRequest) 触发执行
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_assets_compare")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AssetsCompare extends Model<AssetsCompare> implements CompareFile {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("业务关联id")
    private Long bizId;

    @ApiModelProperty("基础表ID")
    private Long baseTableId;

    private Long assetsId;

    @ApiModelProperty("资产 历史版本")
    private Integer preVersion;

    @ApiModelProperty("资产 新版本")
    private Integer curVersion;

    /**
     * 例如 V6(202401) 资产版本+底表期数版本
     */
    private String curVersionPeriod;

    private String preVersionPeriod;

    /**
     * 结果Excel ftp path
     */
    private String resultPath;

    /**
     * @see AssetsCompareTypeEnum
     */
    private String createType;

    /**
     * 资产 对比任务运行状态
     * <p>
     * 注意新版本的ExcelFTP生成完后，才会触发开始跑对比
     *
     * @see AssetsUpgradeStateEnum
     */
    private String state;

    private Boolean deleted;

    /**
     * 启动时间
     */
    private LocalDateTime startTime;
    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    private String runLog;

    @ApiModelProperty("创建人")
    private Long creator;
    @ApiModelProperty("更新人")
    private Long updater;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    public static String buildVersionPeriod(Integer version, String period) {
        if (StringUtils.isBlank(period)) {
            return "V" + version;
        }
        return String.format("V%d(%s)", version, period);
    }
}
