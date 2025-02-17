package com.sinohealth.system.biz.dataassets.dto.bo;

import com.sinohealth.system.biz.dataassets.domain.AssetsFlowBatchDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-06-26 10:31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecFlowParam {
    private Long applicationId;
    /**
     * 触发配置id（标记手动还是自动）
     *
     * @see AssetsFlowBatchDetail#id
     */
    private Long triggerId;
    /**
     * default 走尚书台默认分组
     * ysg 走专用虚拟机分组
     * ysg_prod 物理机分组
     */
    private String workGroup;

    /**
     * 此次工作流执行对应业务说明，只用于告警
     */
    private String taskName;
}
