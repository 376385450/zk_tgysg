package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * @Author Rudolph
 * @Date 2023-08-22 10:42
 * @Desc
 */
@ApiModel(description = "元数据信息表(tg_metadata_info)表实体类")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_metadata_info")
@EqualsAndHashCode(callSuper = false)
public class TgMetadataInfo extends Model<TgMetadataInfo> {
    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("资产ID")
    private Long assetId;

    @ApiModelProperty("元数据绑定id")
    private Integer metaDataId;

    @ApiModelProperty("所属租户")
    private String tenant;

    @ApiModelProperty("所属租户名称")
    @TableField(exist=false)
    private String tenantName;

    @ApiModelProperty("表中文名")
    @TableField(exist=false)
    private String cnName;

    @ApiModelProperty("数据库类型")
    private String databaseType;

    @ApiModelProperty("数据源")
    private String datasource;

    @ApiModelProperty("ip")
    private String ip;

    @ApiModelProperty("port")
    private Integer port;

    @ApiModelProperty("数据库")
    private String metaDataDatabase;

    @ApiModelProperty("schema")
    private String metaSchema;

    @ApiModelProperty("数据表")
    private String metaDataTable;

    @ApiModelProperty("更新人")
    private String updater;

    @ApiModelProperty("更新时间")
    private String updateTime;

    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    public static TgMetadataInfo newInstance() {
        return new TgMetadataInfo();
    }


}
