package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-31 16:55
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class DataPreviewRequest extends GetDataInfoRequestDTO {

    /**
     * 资产版本
     */
    private Integer version;

    public static DataPreviewRequest buildForHead(){
        return new DataPreviewRequest() {{
            setPageNum(1);
            setPageSize(1);
        }};
    }
}
