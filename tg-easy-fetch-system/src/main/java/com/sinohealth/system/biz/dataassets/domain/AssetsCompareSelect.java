package com.sinohealth.system.biz.dataassets.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Kuangcp
 * 2024-07-17 21:35
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_assets_compare_select")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AssetsCompareSelect extends Model<AssetsCompareSelect> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long creator;

    /**
     * 自动生成资产对比
     */
    private String autoAssetsId;
    /**
     * 手动生成资产对比
     */
    private String manualAssetsId;
}
