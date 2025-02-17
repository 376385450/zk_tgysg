package com.sinohealth.system.domain;/**
 * @author linshiye
 */

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sinohealth.system.domain.constant.AsyncTaskConst;
import lombok.Data;

import java.util.Date;

/**
 * 异步任务中心
 *
 * @version 1.0
 * @date 2023-03-07 4:41 下午
 */
@Data
@TableName(value = "async_task", autoResultMap = true)
public class AsyncTask {
    /**
     * 自增id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 创建时间
     */
    private Date time;

    /**
     * 类型 {@link com.sinohealth.system.domain.constant.AsyncTaskConst.Type}
     */
    private Integer type;

    /**
     * 下载地址
     */
    private String url;

    private String filePath;

    private String fileName;

    private String paramJson;

    /**
     * 业务状态 {@link com.sinohealth.system.domain.constant.AsyncTaskConst.Status}
     */
    private Integer status;

    /**
     * 业务类型 {@link com.sinohealth.system.domain.constant.AsyncTaskConst.BUSINESS_TYPE}
     */
    private Integer businessType;

    /**
     * 流转状态
     */
    private Integer flowStatus;

    /**
     * 备注，异常时记录日志人工排查
     */
    private String remark;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除状态 {@link com.sinohealth.system.domain.constant.AsyncTaskConst.DEL_FLAG}
     */
    private Integer delFlag = AsyncTaskConst.DEL_FLAG.NORMAL;

    /**
     * 是否已读
     */
    private Integer readFlag = AsyncTaskConst.ReadFlag.READ;
}
