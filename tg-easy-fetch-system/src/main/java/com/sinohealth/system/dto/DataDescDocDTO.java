package com.sinohealth.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sinohealth.system.domain.constant.ApplicationConst;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-06 15:27
 */
@Data
@JsonNaming
@Accessors(chain = true)
public class DataDescDocDTO implements Serializable {

    private Integer id;

    /**
     * 资产id
     */
    private Long assetsId;

    private String docName;

    @ApiModelProperty("数据指标")
    private String dataQuota;

    @ApiModelProperty("数据说明")
    private DataDescDocDetailItemDTO dataDesc;

    @ApiModelProperty("基础指标")
    private DataDescDocDetailItemDTO baseTarget;

    @Data
    @JsonNaming
    @Accessors(chain = true)
    public static class DataDescDocDetailItemDTO implements Serializable {

        private List<DataDescTargetDTO> list;

        @Data
        @JsonNaming
        @Accessors(chain = true)
        public static class DataDescTargetDTO implements Serializable {
            /**
             * 用于匹配说明的字段名
             */
            private String key;

            private String displayName;

            private String value;
            /**
             * @see ApplicationConst.FieldSource
             */
            private Integer fieldSource;
        }
    }

}
