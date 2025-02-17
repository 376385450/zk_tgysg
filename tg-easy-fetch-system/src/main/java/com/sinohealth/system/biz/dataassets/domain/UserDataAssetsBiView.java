package com.sinohealth.system.biz.dataassets.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.sinohealth.arkbi.param.DeleteType;
import com.sinohealth.arkbi.vo.ViewVo;
import com.sinohealth.system.biz.dataassets.constant.BiViewStateEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 用户数据资产 BI视图 映射关系
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-10-25 16:25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_user_data_assets_bi_view")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class UserDataAssetsBiView implements AssetsVersion {

    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long assetsId;

    private Integer version;

    /**
     * 虚拟字段
     */
    @TableField(updateStrategy = FieldStrategy.NEVER, insertStrategy = FieldStrategy.NEVER)
    private String assetsVersion;

    /**
     * BI 视图id
     *
     * @see ViewVo#id
     */
    private String viewId;

    /**
     * @see DeleteType
     * @see BiViewStateEnum
     */
    private String dataState;
}
