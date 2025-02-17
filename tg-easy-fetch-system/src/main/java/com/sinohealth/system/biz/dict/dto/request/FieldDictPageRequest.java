package com.sinohealth.system.biz.dict.dto.request;

import com.sinohealth.common.enums.dict.FieldUseWayEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-04 15:51
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FieldDictPageRequest extends DictCommonPageRequest{

    /**
     * @see FieldUseWayEnum
     */
    private String useWay;
}
