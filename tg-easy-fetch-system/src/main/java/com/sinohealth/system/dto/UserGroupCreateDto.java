package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author Jingjun
 * @since 2021/4/15
 */
@ApiModel("UserGroupCreateDto")
@Data
public class UserGroupCreateDto {

    @ApiModelProperty("ID")
    private Long groupId;
    @ApiModelProperty("组名")
   private String groupName;
    @ApiModelProperty("用户ID")
    private  Long userId;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("数据ID")
    @Size(min=1)
    private List<Long> dataDirIds;
}
