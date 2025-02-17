package com.sinohealth.system.dto.analysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 关联信息
 *
 * @author linkaiwei
 * @date 2021/08/18 17:12
 * @since 1.4.1.0
 */
@ApiModel("关联信息")
@Data
@Accessors(chain = true)
public class LinkDTO implements Serializable {

    /**
     * 详情见 {@link com.sinohealth.bi.enums.JoinTypeEnum}
     *
     * @date 2021-08-19 14:28:42
     * @since 1.4.1.0
     */
    @ApiModelProperty("关联类型，left左关联，inner内关联，full全关联，right右关联")
    private String joinType;

    @ApiModelProperty("源表ID（唯一ID）")
    private Long source;

    @ApiModelProperty("关联表ID（唯一ID）")
    private Long target;

    @ApiModelProperty("关联字段信息")
    private List<JoinKey> joinKeys;

    // 以下字段是前端用
    private String sources;
    private String targets;


    /**
     * 字段关联信息
     *
     * @author linkaiwei
     * @date 2021/08/18 17:15
     * @since 1.4.1.0
     */
    @Data
    @ApiModel("字段关联信息")
    @Accessors(chain = true)
    public static class JoinKey implements Serializable {

        @ApiModelProperty("源表字段ID")
        private Long sourceKey;

        @ApiModelProperty("英文名称")
        private String sourceFieldName;

        @ApiModelProperty("中文名称")
        private String sourceFieldAlias;

        @ApiModelProperty("关联表字段ID")
        private Long targetKey;

        @ApiModelProperty("英文名称")
        private String targetFieldName;

        @ApiModelProperty("中文名称")
        private String targetFieldAlias;

    }
}
