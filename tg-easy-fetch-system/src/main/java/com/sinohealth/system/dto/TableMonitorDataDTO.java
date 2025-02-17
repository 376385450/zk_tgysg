package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-11 10:39 上午
 */
@Data
@ApiModel("表单监控数据，返回")
@Accessors(chain = true)
public class TableMonitorDataDTO implements Serializable {

    @ApiModelProperty("本表监控数据")
    private TableMonitorTableData tableData;

    @ApiModelProperty("发放数据监控")
    private TableMonitorApplyData applyData;

    @ApiModelProperty("客户监控数据")
    private TableMonitorCustomerData customerData;

    @Data
    @ApiModel("本表监控数据")
    @Accessors(chain = true)
    public static class TableMonitorTableData implements Serializable {

        @ApiModelProperty("pv")
        private Integer pv;

        @ApiModelProperty("uv")
        private Integer uv;

        @ApiModelProperty("本表监控数据趋势")
        private TableMonitorTableDataTrend trend;

        @Data
        @ApiModel("本表监控数据趋势")
        @Accessors(chain = true)
        public static class TableMonitorTableDataTrend implements Serializable {

            @ApiModelProperty("日期")
            private List<String> date;

            @ApiModelProperty("pv")
            private List<Integer> pv;

            @ApiModelProperty("uv")
            private List<Integer> uv;
        }
    }

    @Data
    @ApiModel("发放数据监控")
    @Accessors(chain = true)
    public static class TableMonitorApplyData implements Serializable {

        @ApiModelProperty("累计发放数据")
        private Integer total;

        @ApiModelProperty("近30天发放数据")
        private Integer day30;

        @ApiModelProperty("当前应用中数据")
        private Integer effect;
    }

    @Data
    @ApiModel("客户监控数据")
    @Accessors(chain = true)
    public static class TableMonitorCustomerData implements Serializable {

        @ApiModelProperty("交付客户数据")
        private Integer apply;

        @ApiModelProperty("客户查表数据")
        private Integer view;

        @ApiModelProperty("客户下载数据")
        private Integer download;

        @ApiModelProperty("客户监控数据趋势")
        private TableMonitorCustomerDataTrend trend;

        @Data
        @ApiModel("客户监控数据趋势")
        @Accessors(chain = true)
        public static class TableMonitorCustomerDataTrend implements Serializable {

            @ApiModelProperty("日期")
            private List<String> date;

            @ApiModelProperty("view")
            private List<Integer> view;

            @ApiModelProperty("download")
            private List<Integer> download;
        }
    }

}
