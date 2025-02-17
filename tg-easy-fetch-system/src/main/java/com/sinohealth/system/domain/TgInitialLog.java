package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.common.enums.AssetType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Rudolph
 * @Date 2023-10-08 15:05
 * @Desc
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_initial_log")
public class TgInitialLog  extends Model<TgInitialLog> {
    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("模型/库表/文件/申请")
    private AssetType type;

    @ApiModelProperty("模型id/库表id/文件id/申请id")
    private Long relatedId;

    @ApiModelProperty("数据初始化前JSON")
    private String dataJson;

    @ApiModelProperty("调用者")
    private String creator;

    @ApiModelProperty("创建日期")
    private String createTime;

    @ApiModelProperty("更新日期")
    private String updateTime;

    @ApiModelProperty("初始化状态:0-待初始化,1-已初始化")
    private Integer status;

    public static TgInitialLog newInstance() {
        return new TgInitialLog();
    }

}
