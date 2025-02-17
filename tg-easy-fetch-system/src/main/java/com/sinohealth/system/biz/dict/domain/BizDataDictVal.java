package com.sinohealth.system.biz.dict.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 数据字典值
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-05-08 16:19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_biz_data_dict_val")
@Accessors(chain = true)
public class BizDataDictVal {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long dictId;

    private String val;

    @ApiModelProperty("展示名")
    private String name;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
