package com.sinohealth.system.dto;

import com.sinohealth.common.core.domain.entity.SysCustomer;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 * @author Jingjun
 * @since 2021/4/19
 */
@Data
@ApiModel("UserQuitDto")
public class UserQuitDto {

    @ApiModelProperty("用户ID")
    private Long userId;
    @ApiModelProperty("用户名")
    private String userName;
    @ApiModelProperty("员工姓名")
    private String realName;
    @ApiModelProperty("负责表数")
    private int tableCnt;
    @ApiModelProperty("负责审核流程数")
    private int processCnt;
    @ApiModelProperty("负责项目数")
    private int projectCnt;
    @ApiModelProperty("负责客户数")
    private int customerCnt;
    @ApiModelProperty("分配人")
    private Long newUserId;


}
