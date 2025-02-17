package com.sinohealth.system.dto.customer;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @Author shallwetalk
 * @Date 2024/1/10
 */
@Data
@ApiModel("分页查询客户参数")
public class PageQueryCustomer {

    private String shortName;

    private String fullName;

    private Integer customerType;

    private Integer status;

    private Long pageNum;

    private Long pageSize;

}
