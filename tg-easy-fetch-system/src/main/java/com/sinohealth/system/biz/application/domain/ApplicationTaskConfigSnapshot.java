package com.sinohealth.system.biz.application.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-05 15:36
 */
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "提数申请 自动化工作流 参数快照表")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_application_task_config_snapshot")
@Accessors(chain = true)
public class ApplicationTaskConfigSnapshot extends ApplicationTaskConfig {

    /**
     * 配置id
     */
    private Long configId;
}
