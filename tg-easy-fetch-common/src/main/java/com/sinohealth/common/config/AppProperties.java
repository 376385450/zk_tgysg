package com.sinohealth.common.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-11-12 10:30
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Integer quartzThreadCount;

    private String cascadeFields;

    private String cascadeTable;

    private Long cascadeTableId;

    /**
     * 自定义标签 维度表
     */
    private String tagTable;
    /**
     * 级联字段 数据字典id
     */
    private Long tagCascadeDictId;

    private Integer maxTaskConCount;

    private Integer cmhId;

    /**
     * 客户下拉列表 从字典管理中硬编码关联
     */
    private Long customerId;

    private Integer alertType;

    /**
     * log 请求和响应 body
     */
    private Set<String> bodyLogPath;

    /**
     * 目前主要用于底表同步 工作流
     * <p>
     * 尚书台手动运行 通知策略
     * ALL 失败或成功
     * FAILURE 失败发
     * NONE 不通知
     */
    private String warningType;
    /**
     * 主要用于 出数工作流
     */
    private String handleWarningType;
    /**
     * 尚书台手动运行 告警组
     */
    private Integer warningGroupId;
    /**
     * 尚书台 数据交换 告警组
     */
    private Integer swapWarningGroupId;

    /**
     * 企业微信机器人key 业务导向
     */
    private String wxRobot;

    /**
     * 企业微信机器人key 开发导向
     */
    private String wxDevRobot;

    /**
     * 云上环境:
     * 1. 关闭quartz调度
     * 2. 不能连通尚书台
     */
    private Boolean cloudEnv;

    /**
     * true：备份表 + HDFS
     * false： 都不做
     */
    private Boolean needBackup;

    /**
     * 当是false时 单独控制HDFS备份
     */
    private Boolean needHdfs;

    /**
     * 警告的数据量
     */
    private Integer auditWarnCount;
    /**
     * 尚书台调用易数阁的场景
     * <p>
     * 1. 实例执行结束后的执行结果回调
     * 2. 查询指标库定义 处理资产表字段的精度
     * 3. 资产工作流从GP回推资产表到CK
     */
    private String dolphinCallHost;

    /**
     * 对比资产差异 URL Python实现
     */
    private String assetsCompareUrl;
    /**
     * 外部服务回调时 自身URL
     */
    private String assetsCompareSelfUrl;
    private String fileCompareSelfUrl;

    private String flowWorkGroup;
    /**
     * 宽表最大版本数量 包含最新版本
     */
    private Integer maxVersionedTabCnt;

    /**
     * QC sku 合并表
     */
    private String qcSkuTable;

    private Integer qcFlowId;

    /**
     * 底表出数管理 默认展示表
     */
    private Long defaultCmhTableId;

    /**
     * 模板分布类型字段id
     */
    private Long distributedTypeFieldId;

    /**
     * 模板分布区间字段id
     */
    private Long distributedQjFieldId;

    public List<List<String>> buildCascadeFieldsList() {
        String cascadeFields = this.cascadeFields;
        if (StringUtils.isBlank(cascadeFields)) {
            return Collections.emptyList();
        }
        String[] pairs = cascadeFields.split("#");
        return Arrays.stream(pairs).map(v -> Arrays.stream(v.split(",")).collect(Collectors.toList())).collect(Collectors.toList());
    }

    public boolean isNeedPrint(String path) {
        return bodyLogPath.stream().anyMatch(path::startsWith);
    }

    public String getFinalWarningType() {
        String configType = this.getWarningType();
        return StringUtils.isNotEmpty(configType) ? configType : "NONE";
    }

    public String getFinalHandleWarningType() {
        String configType = this.getHandleWarningType();
        return StringUtils.isNotEmpty(configType) ? configType : "NONE";
    }

    public Integer getFinalWarnGroupId() {
        return getFinalWarnGroupId(0);
    }

    public Integer getFinalWarnGroupId(int id) {
        Integer configId = this.getWarningGroupId();
        return Objects.nonNull(configId) ? configId : id;
    }

    public Integer getFinalSwapWarnGroupId() {
        Integer configId = this.getSwapWarningGroupId();
        return Objects.nonNull(configId) ? configId : 0;
    }

    @Data
    public static class DistributedField {
        /**
         * 分布列字段名称
         */
        private String name;

        /**
         * 分布列字段 描述
         */
        private String description;
    }
}
