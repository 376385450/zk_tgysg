package com.sinohealth.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
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
public class DataDescDocAddReqDTO implements Serializable {

    @NotNull(message = "applicationId不能为空")
    private Long applicationId;

    @NotNull(message = "文档名称不能为空")
    private String docName;

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
            private String key;
            private String value;
        }
    }

}
