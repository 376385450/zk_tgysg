package com.sinohealth.system.domain.customer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sinohealth.common.enums.customer.CustomerTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @Author shallwetalk
 * @Date 2024/1/10
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("t_customer")
public class Customer {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String shortName;

    private String fullName;

    /**
     * @see CustomerTypeEnum
     */
    private Integer customerType;

    private Integer customerStatus;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("更新人")
    private Long updater;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    private Long deleted;

}
