package com.sinohealth.system.biz.dataassets.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.system.biz.ck.constant.CkTableSuffixTable;
import com.sinohealth.system.util.ApplicationSqlUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 09:33
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_assets_pb_push_batch_detail")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class PowerBiPushDetail implements AssetsVersion {

    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long bizId;

    private Long batchId;

    private Long applicationId;
    private Long assetsId;
    private Integer assetsVer;

    /**
     * 注意 常规和通用的方式 表会从分布式表转为单表，需要在执行前做一次判断和替换
     *
     * @see CkTableSuffixTable#SHARD
     * @see CkTableSuffixTable#SNAP
     */
    private String tableName;

    /**
     * PG 表名
     */
    private String pushTableName;

    /**
     * 前置操作 多行SQL 目标端PG执行
     */
    private String preSql;
    /**
     * 数据同步SQL 多行SQL CK端执行
     */
    private String insertSql;

    /**
     * @see AssetsUpgradeStateEnum
     */
    private String state;

    private LocalDateTime startTime;
    private LocalDateTime finishTime;

    private String runLog;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @Override
    public Integer getVersion() {
        return assetsVer;
    }

    public static List<String> parseList(String sql) {
        return Stream.of(sql.split(ApplicationSqlUtil.ROW_SPLIT)).collect(Collectors.toList());
    }

    public static String mergeList(List<String> list) {
        return String.join(ApplicationSqlUtil.ROW_SPLIT, list);
    }
}
