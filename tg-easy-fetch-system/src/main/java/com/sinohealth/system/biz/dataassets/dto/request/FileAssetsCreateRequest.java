package com.sinohealth.system.biz.dataassets.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-18 14:36
 */
@Data
public class FileAssetsCreateRequest {

    @NotNull(message = "请选择项目")
    private Long projectId;

    @NotBlank(message = "请先上传文件")
    private String path;

    @NotBlank(message = "请上传文件")
    private String name;
}
