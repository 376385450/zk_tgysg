package com.sinohealth.system.biz.dataassets.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-15 17:18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAssetsCallbackRequest implements Serializable {

    private Long applicationId;

    /**
     * 结果表名
     */
    private String tableName;
    /**
     * 尚书台实例id
     */
    private String instanceId;

    /**
     * 触发器id
     */
    private Long triggerId;
}
