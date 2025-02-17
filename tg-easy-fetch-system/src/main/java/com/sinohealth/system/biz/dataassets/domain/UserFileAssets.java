package com.sinohealth.system.biz.dataassets.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.system.biz.project.domain.Project;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户私有的文件资产
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-10-18 15:04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_user_file_assets")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class UserFileAssets extends Model<UserFileAssets> {

    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String path;

    private String pdfPath;

    /**
     * @see Project#getId()
     */
    private Long projectId;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
