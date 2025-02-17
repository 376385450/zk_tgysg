package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Jingjun
 * @since 2021/4/15
 */
@Data
@ApiModel("UserGroupDto")
public class UserGroupDto {

    private Long groupId;
    @ApiModelProperty("组名")
   private String groupName;

    private String description;

    private String leader;
    @ApiModelProperty("成员数量")
    private int countMember;

    private Integer status;
}
