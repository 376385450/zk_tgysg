package com.sinohealth.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据目录对象 data_dir
 * 
 * @author jingjun
 * @date 2021-04-16
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("data_dir")
@Accessors(chain = true)
public class DataDir extends Model<DataDir> {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String dirName;

    @TableField(exist = false)
    private Long tableId;

    private Long parentId;

    private Integer datasourceId;

    private String prefix;

    private Integer sort;

    //0删除 1 正常 2 停用
    private Integer status;

    private Date lastUpdate;

    @TableField(exist = false)
    private String dirPath;
    @TableField(exist = false)
    private String idPath;
    @TableField(exist = false)
    private boolean end;

    @TableField(exist = false)
    private String sourceName;

    private Integer target;

    private Long applicantId;

    // TODO 资产id
    @Deprecated
    private Long applicationId;

    private Long nodeId;
    
    private String icon;

    private String comment;

    private Integer moved;

    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    public static DataDir newInstance() {
        return new DataDir();
    }
}
