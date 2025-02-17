package com.sinohealth.system.biz.scheduler.dto.request;

import com.sinohealth.data.intelligence.api.metadata.dto.MetaColumnDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-07 15:20
 */
@Data
public class TypeConvertParam implements Serializable {
    /**
     * 源端数据源类型
     *
     * @see DbType 尚书台
     * @see com.sinohealth.data.intelligence.enums.DataSourceType 元数据
     */
    private String sourceType;

    /**
     * 目标端 数据源类型
     */
    private String targetType;

    private List<MetaColumnDTO> columns;
}
