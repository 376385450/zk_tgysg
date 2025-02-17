package com.sinohealth.system.dto;

import com.sinohealth.common.enums.dataassets.AssetsExpireEnum;
import com.sinohealth.common.enums.dataassets.LatestVersionEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Jingjun
 * @since 2021/4/24
 */
@Data
@ApiModel("ApplicationDataDto")
public class ApplicationDataDto {

    @ApiModelProperty("标题")
    private List<Header> header;
    /**
     * 注意需要确保Map实例为有序Map 和数据库列顺序匹配
     */
    @ApiModelProperty("数据列表")
    private List<LinkedHashMap<String, Object>> list;
    /**
     * 真正执行的SQL 接口暴露，方便调试，业务无关
     */
    private String sql;
    private String tailSql;
    @ApiModelProperty("总数")
    private long total;

    /**
     * @see AssetsExpireEnum
     */
    private String expireType;

    /**
     * 版本升级状态：1 成功未读 2 成功已读 3 失败
     *
     * @see LatestVersionEnum
     */
    private Integer latestVersion;

    private Boolean relateDict;

    @ApiModel("Header")
    @Data
    @AllArgsConstructor
    public static class Header {
        Long id;
        Long tableId;
        String filedName;
        String filedAlias;
        String customName;
        String dataType;
        boolean primaryKey;
        Integer saturability;
        Integer sum;
        Integer nullSum;
        /**
         * 列顺序
         */
        Integer sort;
        /**
         * 自定义树SQL构造出的列
         */
        boolean rangeField;
        String defaultShow;

        public Header(Long id, String filedName, String filedAlias, String dataType) {
            this.id = id;
            this.tableId = 0L;
            this.filedName = filedName;
            this.filedAlias = filedAlias;
            this.dataType = dataType;
        }

        public Header(Long id, Long tableId, String filedName, String filedAlias, String dataType, boolean primaryKey, String defaultShow) {
            this.id = id;
            this.tableId = tableId;
            this.filedName = filedName;
            this.filedAlias = filedAlias;
            this.dataType = dataType;
            this.primaryKey = primaryKey;
            this.defaultShow = defaultShow;
        }
    }
}
