package com.sinohealth.api.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/11/13
 */
@Data
public class UserDeleteDto implements Serializable {

    private Long tgUserId;

}

