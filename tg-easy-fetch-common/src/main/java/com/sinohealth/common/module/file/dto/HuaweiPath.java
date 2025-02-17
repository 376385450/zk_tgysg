package com.sinohealth.common.module.file.dto;

import java.util.Date;

/**
 * Huawei OBS 存储路径对象
 *
 * @author wl
 * @version v1.0
 * @date 2021/7/19
 */
public class HuaweiPath implements BasePath {

    private String sourcePath;

    private String thumbPath;

    private String storageName;

    private String url;

    private String thumbUrl;

    private String fileName;

    private Long fileSize;

    private Date urlExpiryDate;

    private Long urlExpire;


    @Override
    public String getSourcePath() {
        return this.sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    @Override
    public String getThumbPath() {
        return this.thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    @Override
    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public Date getUrlExpiryDate() {
        return this.urlExpiryDate;
    }

    public void setUrlExpiryDate(Date urlExpiryDate) {
        this.urlExpiryDate = urlExpiryDate;
    }

    public void setUrlExpire(Long urlExpire) {
        this.urlExpire = urlExpire;
    }

    public Long getUrlExpire() {
        return urlExpire;
    }

    /**
     * builder
     */
    public static final class Builder {

        private String sourcePath;
        private String thumbPath;
        private String storageName;
        private String url;
        private String thumbUrl;
        private String fileName;
        private Long fileSize;
        private Date urlExpiryDate;
        private Long urlExpire;


        public Builder withSourcePath(String sourcePath) {
            this.sourcePath = sourcePath;
            return this;
        }

        public Builder withThumbPath(String thumbPath) {
            this.thumbPath = thumbPath;
            return this;
        }

        public Builder withStorageName(String storageName) {
            this.storageName = storageName;
            return this;
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withThumbUrl(String thumbUrl) {
            this.thumbUrl = thumbUrl;
            return this;
        }

        public Builder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder withFileSize(Long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public Builder withUrlExpiryDate(Date urlExpiryDate) {
            this.urlExpiryDate = urlExpiryDate;
            return this;
        }
        public Builder withUrlExpire(Long urlExpire) {
            this.urlExpire = urlExpire;
            return this;
        }

        public HuaweiPath build() {
            HuaweiPath huaweiPath = new HuaweiPath();
            huaweiPath.setSourcePath(sourcePath);
            huaweiPath.setThumbPath(thumbPath);
            huaweiPath.setStorageName(storageName);
            huaweiPath.setUrl(url);
            huaweiPath.setThumbUrl(thumbUrl);
            huaweiPath.setFileName(fileName);
            huaweiPath.setFileSize(fileSize);
            huaweiPath.setUrlExpiryDate(urlExpiryDate);
            huaweiPath.setUrlExpire(urlExpire);
            return huaweiPath;
        }
    }



}
