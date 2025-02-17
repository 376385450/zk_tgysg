package com.sinohealth.system.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.*;


/**
 * API接口详情响应数据
 *
 * @author linkaiwei
 * @date 2021/07/16 11:21
 * @since dev
 */
@Data
@ApiModel("API接口详情响应数据")
public class GetApiInfoDetailResponse implements Serializable {

    @ApiModelProperty("APISchemaId")
    private String apiSchemaId;

    @ApiModelProperty("API接口名称")
    private String apiName;

    @ApiModelProperty("API接口描述")
    private String apiDesc;

    @ApiModelProperty("API分组ID")
    private String apiGroupId;

    @ApiModelProperty("API请求方式")
    private String methodType;

    @ApiModelProperty("API请求方式描述")
    private String methodTypeDesc;

    @ApiModelProperty("更新人")
    private String apiUrl;

    @ApiModelProperty("API响应参数")
    private ResultParam resultYapiSchema;

    @ApiModelProperty("API请求头部")
    private List<HeaderParam> paramHeaderYapiSchema = Collections.singletonList(new HeaderParam());

    @ApiModelProperty("API请求参数（GET）")
    private List<QueryParam> paramQueryYapiSchema = Arrays.asList(
            new QueryParam("page", "", true, "当前页码", "1",
                    new QueryParam.Limited(null, null, null, "number")),
            new QueryParam("size", "", true, "一页显示条数", "10",
                    new QueryParam.Limited(null, null, null, "number")));

    @ApiModelProperty("API请求参数（POST）")
    private Param paramBodyYapiSchema = new Param("object", null, null, null,
            new JSONObject()
                    .fluentPut("page", new Param("number", "当前页码", "1", true))
                    .fluentPut("size", new Param("number", "一页显示条数", "10", true))
                    .getInnerMap(),
            null, new String[]{"page", "size"});

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("API接口版本")
    private String apiVersion;

    // POST
    @ApiModelProperty("API接口创建人")
    private String apiCreateBy;

    @ApiModelProperty("API接口创建时间")
    private Date apiCreateTime;

    @ApiModelProperty("API接口类型")
    private String apiType;

    @ApiModelProperty("更新周期")
    private String apiCycle;


    /**
     * 响应参数
     *
     * @author linkaiwei
     * @date 2021/07/16 15:00
     * @since dev
     */
    @Data
    public static class ResultParam implements Serializable {

        private String type = "object";

        private String description = "";

        private Boolean disabled = true;

        private Properties properties = new Properties();


        @Data
        public static class Properties implements Serializable {

            private Param code = new Param("number", "查询结果码", 2000, true);

            private Param desc = new Param("string", "查询结果描述", "是否是第一页", true);

            private Param data;

            private PageInfo pageInfo = new PageInfo();
        }
    }

    @Data
    public static class PageInfo implements Serializable {

        private String type = "object";

        private Map<String, Object> properties = new JSONObject()
                .fluentPut("first", new Param("boolean", "是否是第一页", "false", true))
                .fluentPut("last", new Param("boolean", "是否最后一页", "false", true))
                .fluentPut("number", new Param("number", "当前页", "false", true))
                .fluentPut("size", new Param("number", "当前页记录数", "false", true))
                .fluentPut("totalPages", new Param("number", "总页数", "false", true))
                .fluentPut("totalSize", new Param("number", "总条数", "false", true))
                .getInnerMap();

        private Boolean disabled = true;
    }

    /**
     * 头部参数
     *
     * @author linkaiwei
     * @date 2021/07/16 15:00
     * @since dev
     */
    @Data
    public static class HeaderParam implements Serializable {

        private String name = "Content-Type";

        private Boolean checked = true;

        private String value = "application/json;charset=UTF-8";

        private Boolean required = true;

        private String desc = "";

        private String example = "application/json;charset=UTF-8";
    }

    /**
     * GET请求参数
     *
     * @author linkaiwei
     * @date 2021/07/16 15:00
     * @since dev
     */
    @Data
    public static class QueryParam implements Serializable {

        private String name;

        private String value;

        private Boolean required;

        private String desc;

        private String example;

        private Limited limited;


        public QueryParam(String name, String value, Boolean required, String desc, String example, Limited limited) {
            this.name = name;
            this.value = value;
            this.required = required;
            this.desc = desc;
            this.example = example;
            this.limited = limited;
        }

        @Data
        public static class Limited implements Serializable {

            private Integer minLength;

            private Integer maxLength;

            private Integer itemIndex;

            private String type;

            public Limited(Integer minLength, Integer maxLength, Integer itemIndex, String type) {
                this.minLength = minLength;
                this.maxLength = maxLength;
                this.itemIndex = itemIndex;
                this.type = type;
            }
        }
    }


    /**
     * 参数
     *
     * @author linkaiwei
     * @date 2021/07/16 15:00
     * @since dev
     */
    @Data
    public static class Param implements Serializable {

        private String type;

        private String description;

        private Object defaultValue;

        private Boolean disabled;

        private Map<String, Object> properties;

        private Param items;

        private String[] required;


        public Param() {
        }

        public Param(String type, String description, Object defaultValue, Boolean disabled) {
            this.type = type;
            this.description = description;
            this.defaultValue = defaultValue;
            this.disabled = disabled;
        }

        public Param(String type, String description, Param items) {
            this.type = type;
            this.description = description;
            this.items = items;
        }

        public Param(String type, Map<String, Object> properties, String[] required) {
            this.type = type;
            this.properties = properties;
            this.required = required;
        }

        public Param(String type, String description, Object defaultValue, Boolean disabled, Map<String, Object> properties, Param items, String[] required) {
            this.type = type;
            this.description = description;
            this.defaultValue = defaultValue;
            this.disabled = disabled;
            this.properties = properties;
            this.items = items;
            this.required = required;
        }
    }

}
