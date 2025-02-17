package com.sinohealth.api.user.dto.userSync;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @Author shallwetalk
 * @Date 2023/10/11
 */
@Data
public class TgUser {

    private String id;

    private String tenantCode;

    private String tenantName;

    private String account;

    private String phone;

    private String email;

    private String realName;

    private String pwd;

    private String orgId;

}
