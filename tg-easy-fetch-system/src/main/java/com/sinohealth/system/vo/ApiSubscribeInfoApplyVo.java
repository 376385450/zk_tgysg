package com.sinohealth.system.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * api接口订阅信息对象 api_subscribe_info
 *
 * @author dataplatform
 * @date 2021-07-05
 */
@ApiModel
@Data
public class ApiSubscribeInfoApplyVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @ApiModelProperty("订阅id")
    private Long id;

    /**
     * api发布后的接口ID
     */
    @ApiModelProperty("api服务id")
    private Long apiVersionId;

    public ApiSubscribeInfoApplyVo() {
    }

    public ApiSubscribeInfoApplyVo(Long id, Long apiVersionId) {
        this.id = id;
        this.apiVersionId = apiVersionId;
    }
}
