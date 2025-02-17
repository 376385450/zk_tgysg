package com.sinohealth.system.biz.dataassets.dto;

import com.sinohealth.system.biz.dataassets.domain.AssetsVersion;
import com.sinohealth.system.domain.constant.UpdateRecordStateType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-20 19:01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDataAssetsSyncDTO implements AssetsVersion {

    private Long assetsId;
    private String assetsName;
    private Integer version;

    @ApiModelProperty("BI报表数")
    private Integer chartCount;
    /**
     * 状态
     * @see UpdateRecordStateType
     */
    private Integer state;
    @ApiModelProperty("过期时间")
    private LocalDateTime dataExpire;
    @ApiModelProperty("资产创建时间")
    private LocalDateTime createTime;

}
