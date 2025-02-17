package com.sinohealth.system.biz.dict.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author Kuangcp
 * 2024-08-13 15:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_kv_dict")
@Accessors(chain = true)
public class KeyValDict extends Model<KeyValDict> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String val;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
