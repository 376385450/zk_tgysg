package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 19:16
 */
@Data
@TableName("tg_deliver_customer_record")
public class TgDeliverCustomerRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long assetsId;

    private Long tableId;

    private String tableName;

    /**
     * 1打包交付，0非打包交付
     */
    private Integer allocateType;

    private Long allocateUserId;

    private String allocateUserName;

    private String authType;

    private String projectName;

    private String packName;

    /**
     * parentRecordId null 表示根节点
     */
    private Integer parentRecordId;

    private Long nodeId;

    private String icon;

    private Long createBy;

    private Date createTime;
}
