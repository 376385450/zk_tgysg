package com.sinohealth.system.domain.asset;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author shallwetalk
 * @Date 2023/10/30
 */
@Data
@TableName("tg_asset_info_relate")
@Accessors(chain = true)
public class TgAssetRelate {

    @TableId
    private Long id;

    private Long assetId;

    private Long relateAssetId;

    private Integer relateSort;

}
