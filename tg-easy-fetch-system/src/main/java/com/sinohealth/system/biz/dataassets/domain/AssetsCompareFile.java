package com.sinohealth.system.biz.dataassets.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.common.core.domain.IdTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author Kuangcp
 * 2024-07-17 16:05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_assets_compare_file")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AssetsCompareFile extends Model<AssetsCompareFile> implements CompareFile, IdTable {


    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 新版 品类 逗号分隔多个
     */
    private String prodCode;

    /**
     * 新版 数据期数
     */
    private String dataPeriod;

    private String newFileName;
    private String newPath;
    private String oldFileName;
    private String oldPath;
    private String resultPath;
    private String state;
    private String runLog;

    private Boolean deleted;

    private Long creator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private LocalDateTime startTime;
    private LocalDateTime finishTime;

}
