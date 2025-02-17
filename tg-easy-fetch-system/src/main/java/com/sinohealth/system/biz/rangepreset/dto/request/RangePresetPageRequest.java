package com.sinohealth.system.biz.rangepreset.dto.request;

import com.sinohealth.system.biz.dict.dto.request.DictCommonPageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-24 14:56
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RangePresetPageRequest extends DictCommonPageRequest {

    /**
     * 模板id
     */
    private Long templateId;

    /**
     * 是否 使用时的查询（区别于管理页的查询）
     */
//    private Boolean use;
}
