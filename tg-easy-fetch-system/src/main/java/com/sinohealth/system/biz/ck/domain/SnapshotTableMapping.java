package com.sinohealth.system.biz.ck.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.system.biz.ck.constant.SnapshotTableHdfsStateEnum;
import com.sinohealth.system.biz.ck.constant.SnapshotTableStateEnum;
import com.sinohealth.system.biz.ck.dto.CkDataSource;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 快照表和CK集群节点映射关系
 * @author kuangchengping@sinohealth.cn 
 * 2023-12-01 16:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_snapshot_table_mapping")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class SnapshotTableMapping extends Model<SnapshotTableMapping> {

    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String tableName;

    /**
     * 数据主节点 hostname
     * @see CkDataSource#hostName
     */
    private String host;

    /**
     * TODO 是否需要第三个及以上的备份节点
     * 备份节点 hostname
     * @see CkDataSource#hostName
     */
    private String candidateHost;

    /**
     * @see SnapshotTableStateEnum
     */
    private String state;

    /**
     * @see SnapshotTableHdfsStateEnum
     */
    private String hdfsState;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
