package com.sinohealth.system.biz.project.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-30 09:50
 */
@Data
public class ProjectUpsertParam {

    private Long id;

    @NotBlank(message = "项目名称未填")
    @Length(max = 32, message = "项目名超出{max}位")
    private String name;

    @Length(max = 256, message = "项目说明超出{max}位")
    private String description;

    private Long customerId;

    private String customerShortName;

    private Integer customerType;

    private Long projectManager;

    private List<Long> collaborationUser;

    private List<Long> relateAsset;

    private Integer status;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("更新人")
    private String updater;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

}
