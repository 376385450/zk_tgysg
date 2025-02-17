package com.sinohealth.system.dto;

import com.sinohealth.common.enums.LogType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author Jingjun
 * @since 2021/5/25
 */
@Getter
@Setter
@ApiModel("UserQueryTableLogDto")
public class UserQueryTableLogDto {
    private Long userId;
    @ApiModelProperty("用户名")
    private String userName;
    @ApiModelProperty("角色名")
    private String roleName;
    @ApiModelProperty("操作时间")
    private Date createTime;
    @ApiModelProperty("操作类型")
    private Integer logType;
    @ApiModelProperty("操作类型中文")
    public String getLogTypeName(){
        return LogType.findName(logType);
    }
}
