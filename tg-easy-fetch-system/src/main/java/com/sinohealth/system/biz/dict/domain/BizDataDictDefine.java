package com.sinohealth.system.biz.dict.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.enums.dict.DataDictDataTypeEnum;
import com.sinohealth.common.enums.dict.DataDictEnum;
import com.sinohealth.system.biz.dict.service.UniqueDomain;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 业务数据字典
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-05-05 13:50
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_biz_data_dict_define")
@Accessors(chain = true)
public class BizDataDictDefine implements UniqueDomain<BizDataDictDefine> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("字典名称")
    private String name;

    @ApiModelProperty("字典说明")
    private String description;

    /**
     * @see DataDictEnum
     */
    @ApiModelProperty("字典 配置方式")
    private String dictType;

    /**
     * @see DataDictDataTypeEnum
     */
    @ApiModelProperty("数据类型")
    private String dataType;

    @ApiModelProperty("系统字典")
    private Boolean systemDict;

    /**
     * @see BizTypeEnum
     */
    @ApiModelProperty("业务线")
    private String bizType;

    @ApiModelProperty("维表引用")
    private String quoteSql;
    /**
     * 从SQL中解析得到，自动将同表的不同字段，组装出级联筛选配置表，实现级联功能
     * 注意：忽略别名 计算函数 等特殊情况带来的级联业务不可用 业务方负责维护准确性
     */
    private String quoteCol;
    private String quoteTable;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("更新人")
    private Long updater;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @Override
    public String getBizName() {
        return String.format("%s", name);
    }

    @Override
    public void appendQuery(LambdaQueryWrapper<BizDataDictDefine> wrapper) {
        wrapper.or(v -> v.eq(BizDataDictDefine::getName, this.getName()));
    }
}
