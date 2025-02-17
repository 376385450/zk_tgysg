package com.sinohealth.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.annotation.Excel;
import com.sinohealth.common.annotation.Excel.ColumnType;
import com.sinohealth.common.annotation.Excel.Type;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户对象 sys_user
 * 
 * @author dataplatform
 */

@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("sys_user")
public class SysUser implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Excel(name = "用户序号", cellType = ColumnType.NUMERIC, prompt = "用户编号")
    @TableId(value = "user_id",type = IdType.AUTO)
    private Long userId;

    /** 用户账号 */
    @NotBlank(message = "用户账号不能为空")
    @Size(min = 0, max = 30, message = "用户账号长度不能超过30个字符")
    @Excel(name = "登录名称")
    private String userName;

    /** 用户昵称 */
    @Size(min = 0, max = 30, message = "用户昵称长度不能超过30个字符")
    @Excel(name = "用户名称")
    private String realName;

    /** 用户邮箱 */
    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
    @Excel(name = "用户邮箱")
    private String email;

    /** 手机号码 */
    @Size(min = 0, max = 11, message = "手机号码长度不能超过11个字符")
    @Excel(name = "手机号码")
    private String phonenumber;

    /** 用户头像 */
    private String avatar;

    /** 密码 */
    @JsonIgnore
    private String password;

    /** 帐号状态（0正常 1停用） */
    @Excel(name = "帐号状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    @TableLogic
    private String delFlag;

    /** 最后登录时间 */
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.EXPORT)
    private Date loginDate;

    private Integer loginTimes;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 备注 */
    private String remark;

    /** token*/
    private String token;

    @TableField(value = "org_user_id")
    private String orgUserId;

    /**
     * 用户类型：1 未区分 2 内部人员 3 客户
     */
    private Integer userInfoType;

    private  Integer isSubAccount;

    private  Long parentAccountId;

    /**
     * 请求参数
     */
    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();

    /** 角色对象 */
    @TableField(exist = false)
    private List<SysRole> roles;

    @TableField(exist = false)
    private List<SysGroup> groups;

    @TableField(exist = false)
    private SysCustomer sysCustomer;

    @TableField(exist = false)
    private String  roleName;

    @TableField(exist = false)
    private Integer  subAccountCnt;

    @TableField(exist = false)
    private Integer  viewTableCnt;

    @TableField(exist = false)
    private SinoPassUserDTO sinoPassUserDTO;

    /** 创建者带部门信息 */
    @TableField(exist = false)
    private String createByOri;

    public SysUser(Long userId)
    {
        this.userId = userId;
    }


    /**
     * 获取客户真实姓名
     *
     * @param userMap 用户信息Map
     * @param userId  用户ID
     * @return 用户真实姓名
     * @author linkaiwei
     * @date 2021-09-02 15:00:04
     * @since 1.4.2.0
     */
    public static String getRealName(Map<Long, SysUser> userMap, Long userId) {
        if (userMap == null || userMap.isEmpty() || userId == null) {
            return null;
        }
        final SysUser sysUser = userMap.get(userId);
        if (sysUser == null) {
            return null;
        }
        return sysUser.getRealName();
    }
}
