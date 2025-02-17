package com.sinohealth.system.biz.table.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sinohealth.common.core.domain.IdTable;
import com.sinohealth.common.enums.process.FlowProcessUpdateType;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import com.sinohealth.system.domain.TableInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 包含最新版本
 *
 * @author kuangchengping@sinohealth.cn
 * 2024-04-18 14:17
 * @see TableInfo
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("tg_table_info_snapshot")
public class TableInfoSnapshot implements Serializable, IdTable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private Long bizId;

    /**
     * @see com.sinohealth.system.domain.TableInfo#id
     */
    private Long tableId;

    @ApiModelProperty("表英文名")
    private String tableName;

    @ApiModelProperty("分布式表名")
    private String tableNameDistributed;

    @ApiModelProperty("数据行数")
    private Long totalRow;

    @ApiModelProperty("说明")
    private String remark;

    /**
     * @see FlowProcessTypeEnum
     */
    private String flowProcessType;

    /**
     * 关联品类 , 拼接
     */
    private String prodCodes;

    /**
     * @see FlowProcessUpdateType
     */
    private String updateType;

    @ApiModelProperty("delete 删除 normal 可用")
    private String status;

    /**
     * @see com.sinohealth.system.biz.table.constants.TablePushStatusEnum
     */
    @ApiModelProperty("none 未推送资产 run 执行中 success failed")
    private String pushStatus;

    @ApiModelProperty("推送资产时 选择的对比历史版本")
    private Integer preVersion;

    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    /**
     * 是否最新版本
     */
    private Boolean latest;
    /**
     * 版本
     * <p>
     * 关联 虚拟字段 table_version
     */
    private Integer version;
    /**
     * 期数版本
     */
    private String versionPeriod;
    /**
     * 数据同步时间
     */
    private LocalDateTime syncTime;

    private Long createBy;

    public String buildTableVersion() {
        return tableId + "#" + version;
    }
}
