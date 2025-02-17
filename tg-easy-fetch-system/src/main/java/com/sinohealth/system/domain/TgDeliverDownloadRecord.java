package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 分配客户报表 - 下载记录
 *
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 13:56
 */
@Data
@TableName("tg_deliver_download_record")
public class TgDeliverDownloadRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long assetsId;

    private Long tableId;

    private String tableName;

    /**
     * 1打包交付，0非打包交付
     */
    private Integer allocateType;

    private String projectName;

    private String packName;

    /**
     * parentRecordId null 表示根节点
     */
    private Integer parentRecordId;

    private Long nodeId;

    private String icon;

    private String downloadType;

    private Date downloadTime;

    private Long createBy;

    private Date createTime;
}
