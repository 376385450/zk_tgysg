package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * @Author shallwetalk
 * @Date 2023/9/11
 */
@ApiModel(description = "资产埋点表(tg_asset_burial_point)表实体类")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_asset_burial_point")
@EqualsAndHashCode(callSuper = false)
public class TgAssetBurialPoint{


    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("asset_id")
    private Long assetId;

    @TableField("view_num")
    private Integer viewNum;

    @TableField("burial_date")
    private Date burialDate;

}
