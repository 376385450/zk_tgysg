package com.sinohealth.system.dto.system;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2023-03-09 3:52 下午
 */
@Data
@ApiModel("异步任务Vo")
public class AsyncTaskVo {
    /**
     * 自增id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ApiModelProperty("项目名称")
    private String projectName;

    /**
     * 创建时间
     */
    @ApiModelProperty("时间")
    private Date time;

    private Integer businessType;

    private String paramJson;

    /**
     * 下载地址
     */
    @ApiModelProperty("下载地址，不为空显示下载按钮")
    private String url;

    /**
     * 状态 {@link com.sinohealth.system.domain.constant.AsyncTaskConst.Status}
     */
    @ApiModelProperty("0 已完成、1 进行中、2 失败")
    private Integer status;

    @ApiModelProperty("3 CSV、4、excel 5、zip")
    private Integer type;

    @ApiModelProperty("文件路径")
    private String filePath;

    @ApiModelProperty("文件名")
    private String fileName;

    @ApiModelProperty("是否已读")
    private Integer readFlag;
}
