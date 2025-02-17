package com.sinohealth.system.dto.assets;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-07 13:46
 */
@Data
@JsonNaming
public class AssetsAuthStatusUpdateReqDTO implements Serializable {

    @NotNull(message = "授权id不能为空")
    private Long authId;

    @NotNull(message = "status不能为空")
    private Integer status;
}
