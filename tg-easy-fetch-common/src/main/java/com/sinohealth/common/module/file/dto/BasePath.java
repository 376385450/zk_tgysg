package com.sinohealth.common.module.file.dto;

import java.util.Date;

/**
 * 基础文件存储路径
 *
 * @author linkaiwei
 * @version v1.0
 * @date 2020/4/9 14:53
 */
public interface BasePath {

    /**
     * 源文件存储路径
     */
    String getSourcePath();

    /**
     * 图片缩略图存储路径，图片类型才有，其它类型为空
     */
    String getThumbPath();

    /**
     * 获取存储文件存储位置名称
     */
    String getStorageName();

    /**
     * 获取文件访问链接
     */
    String getUrl();

    /**
     * 获取图片缩略图访问链接
     */
    String getThumbUrl();

    /**
     * 获取源文件名称
     */
    String getFileName();

    /**
     * 获取源文件大小
     */
    Long getFileSize();

    /**
     * 获取链接有效期
     */
    Date getUrlExpiryDate();

}

