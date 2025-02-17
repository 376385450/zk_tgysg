package com.sinohealth.system.dto.analysis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 数据源列表，响应参数
 *
 * @author linkaiwei
 * @date 2021/8/16 17:09
 * @since 1.4.1.0
 */
@Data
@ApiModel("数据源列表，响应参数")
@Accessors(chain = true)
public class ListDatasourceResponseDTO implements Serializable {

    @ApiModelProperty("唯一id")
    private Long uniqueId;

    @ApiModelProperty("表ID")
    private Long id;

    @ApiModelProperty("表英文名")
    private String tableName;

    @ApiModelProperty("表中文名")
    private String tableAlias;

    @ApiModelProperty("数据目录ID")
    private Long dirId;

    @ApiModelProperty("数据源ID")
    private Long sourceId;

    @ApiModelProperty("表单所属库名")
    private String sourceName;

    @ApiModelProperty("字段信息列表")
    private List<TableFieldInfoDTO> fieldInfoList;


    /**
     * 字段信息
     *
     * @author linkaiwei
     * @date 2021/08/16 17:14
     * @since 1.4.1.0
     */
    @Data
    public static class TableFieldInfoDTO implements Serializable {

        @ApiModelProperty("字段ID")
        private Long id;

        @ApiModelProperty("字段名称")
        private String fieldName;

        @ApiModelProperty("字段中文名称")
        private String fieldAlias;

        @ApiModelProperty("字段长度")
        private int length;

        @ApiModelProperty("小数位数")
        private int scale;

        @ApiModelProperty("是否主键")
        private boolean primaryKey;

        @ApiModelProperty("是否非空")
        private boolean empty;

        @ApiModelProperty("总段备注")
        private String comment;

        @ApiModelProperty("唯一id")
        private Long uniqueId;

        @ApiModelProperty("表ID")
        private Long tableId;

        @ApiModelProperty("数据目录ID")
        private Long dirId;

        /**
         * 详情见 {@link com.sinohealth.bi.enums.SqlTypeEnum}
         *
         * @date 2021-08-19 14:33:46
         * @since 1.4.1.0
         */
        @ApiModelProperty("数据库表字段类型")
        private String sqlType;

    }

}
