package com.sinohealth.api.user.dto;

import com.sinohealth.common.core.domain.entity.SysCustomer;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.system.dto.DataDirDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author Jingjun
 * @since 2021/4/19
 */
@Data
@ApiModel("UserCreateDto")
public class UserCreateDto {

    @ApiModelProperty("用户ID")
    private Long userId;
    @ApiModelProperty("用户名")
    @NotNull(message = "用户名不能为空")
    private String userName;
    @ApiModelProperty("实名")
    private String realName;
    @ApiModelProperty("邮件")
    private String email;
    @ApiModelProperty("电话号码")
    private String phonenumber;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("最后登录时间")
    private Date lastTime;
    @ApiModelProperty("累计登录次数")
    private Integer loginNum;
    @ApiModelProperty("账号状态（0正常 1停用）")
    private Integer status;
    @ApiModelProperty("组织员工ID")
    private String orgUserId;
    @ApiModelProperty("用户类型：1 未区分 2 内部人员 3 客户")
    @NotNull(message = "账号类型必填")
    private Integer userInfoType;
    @ApiModelProperty("用户token")
    private String token;
    @ApiModelProperty("角色ID")
    private List<Long> roleIds;
    @ApiModelProperty("分组ID")
    private List<Long> groupIds;
    @ApiModelProperty("树状表权限")
    private DataDirDto tree;

    @ApiModelProperty("菜单ID")
    private List<Long> menus;

    @ApiModelProperty("客户信息")
    private SysCustomer sysCustomer;

    @ApiModelProperty("父账号id")
    private  Long parentAccountId;

    @ApiModelProperty("员工状态")
    private  String employeeStatusText;

    @ApiModelProperty("员工带部门机构信息")
    private SinoPassUserDTO sinoPassUserDTO;

    @ApiModelProperty("客户分类")
    private String customerType;

    @ApiModelProperty("天宫用户id")
    private Long tgUserId;

    @ApiModelProperty("创建人")
    private String creator;

}
