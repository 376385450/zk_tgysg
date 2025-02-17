package com.sinohealth.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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
public class DataDescDocUpdateReqDTO implements Serializable {

    @NotNull(message = "资产id不能为空")
    private Long assetsId;

    @NotNull(message = "文档名称不能为空")
    private String docName;

    @ApiModelProperty("数据指标")
    private String dataQuota;

    @Valid
    @ApiModelProperty("数据说明")
    private DataDescDocDetailItemDTO dataDesc;

    @Valid
    @ApiModelProperty("基础指标")
    private DataDescDocDetailItemDTO baseTarget;

    @Data
    @JsonNaming
    @Accessors(chain = true)
    public static class DataDescDocDetailItemDTO implements Serializable {

        @Valid
        private List<DataDescTargetDTO> list;

        @Data
        @JsonNaming
        @Accessors(chain = true)
        public static class DataDescTargetDTO implements Serializable {

            @NotBlank(message = "key不能为空")
            private String key;
            private String displayName;
            @NotBlank(message = "存在部分值未填写，请根据需要填写完整或删除相关内容")
            private String value;
        }
    }

}
