package com.sinohealth.system.biz.ck.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2023-12-07 13:53
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_snapshot_table_mapping_detail")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class SnapshotTableMappingDetail extends Model<SnapshotTableMappingDetail> {

    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long mapId;


    private String ddl;
}
