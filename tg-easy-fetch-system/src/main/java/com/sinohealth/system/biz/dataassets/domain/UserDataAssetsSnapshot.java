package com.sinohealth.system.biz.dataassets.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-05 11:50
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_user_data_assets_snapshot")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class UserDataAssetsSnapshot extends UserDataAssets {

    /**
     * @see UserDataAssets#id
     */
    private Long assetsId;

}
