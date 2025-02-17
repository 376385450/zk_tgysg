package com.sinohealth.system.dto.analysis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sinohealth.bi.constant.SqlConstant;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.system.biz.dict.domain.FieldDict;
import com.sinohealth.system.domain.TableFieldInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 过滤信息
 * <p>
 * 注意 当前递归实现为 三层结构的 最小递归单元(filters 嵌套 filters（此层仅起到连接作用） 嵌套 filterItem)
 * 理论上可以简化为  filterItem 嵌套 filters
 *
 * @author linkaiwei
 * @date 2021/08/18 17:15
 * @since 1.4.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@ApiModel("过滤信息")
@Accessors(chain = true)
public class FilterDTO implements Serializable {

    @ApiModelProperty("前端组件字段")
    private String id;

    @ApiModelProperty("逻辑操作，and且，or或")
    private String logicalOperator;

    @ApiModelProperty("中文名称")
    private List<FilterDTO> filters;

    @ApiModelProperty("字段信息")
    private FilterItemDTO filterItem;

    @ApiModelProperty("前端组件字段")
    private Integer isFather;

    @ApiModelProperty("前端组件字段")
    private String fatherId;

    /**
     * 中间值字段
     */
    @JsonIgnore
    private Boolean bracket;

//    @ApiModelProperty("前端组件字段 是否展示")
//    private Boolean show;

    /**
     * 过滤项目信息
     *
     * @author linkaiwei
     * @date 2021/08/19 18:22
     * @since 1.4.1.0
     */
    @Data
    @ApiModel("过滤项目信息")
    @Accessors(chain = true)
    public static class FilterItemDTO implements Serializable {

        @ApiModelProperty("前端组件字段")
        private String id;

        @ApiModelProperty("唯一id")
        private Long uniqueId;

        /**
         * @see com.sinohealth.system.domain.TableInfo#id
         */
        @ApiModelProperty("表ID")
        private Long tableId;

        private String tableAlias;

        /**
         * 物理表字段 和 字段库字段
         *
         * @see TableFieldInfo#id
         * @see FieldDict#id
         */
        @ApiModelProperty("字段ID")
        private Long fieldId;

        @ApiModelProperty("英文名称")
        private String fieldName;

        @ApiModelProperty("中文名称")
        private String fieldAlias;

        @ApiModelProperty("逻辑操作，and且，or或")
        private String andOr;

        /**
         * 详情见 {@link com.sinohealth.bi.enums.FunctionalOperatorEnum}
         */
        @ApiModelProperty("功能操作，equalTo等于；notEqualTo不等于；contain包含；notContain不包含；empty为空；notEmpty不为空；moreThan大于；moreThanOrEqualTo大于等于；lessThan小于；lessThanOrEqualTo小于等于；range查询范围(a,b)；rangeLeft查询范围[a,b)；rangeRight查询范围(a,b]；rangeAll查询范围[a,b]；")
        private String functionalOperator;

        /**
         * @see SqlConstant#IN_OR_REG 等于时, 分隔多个值
         * @see SqlConstant#IN_OR 等于时, 分隔多个值
         */
        @ApiModelProperty("过滤值")
        private Object value;

        @ApiModelProperty("另一个过滤值（查询范围）")
        private String otherValue;

        /**
         * @see CommonConstants#TEMPLATE
         */
        @ApiModelProperty("来源标识: 1 - 模板, 2 - 申请")
        private Integer isItself = 2;

        @ApiModelProperty("中文名称")
        private List<FilterDTO> filters;

        @ApiModelProperty("时间维度, 前端使用字段")
        private String timeDimension;

        @ApiModelProperty("时间展示字段, 前端展示用")
        private Object timeViewName;

        /**
         * 附加字段
         */
        private String copyfieldNamePlaceholder;

        /**
         * 附加字段
         */
        private String functionalOperatorPlaceholder;

        /**
         * 附加字段
         */
        private String noMatchValue;
    }
}
