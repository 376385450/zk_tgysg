package com.sinohealth.system.dto.pool;

import lombok.Data;

import java.util.Date;


@Data
public class SubcriBeUserResponse {

    private Long id;

    private Long userId;

    private String userName;

    private String groupNames;

    private Integer userCount;

}
