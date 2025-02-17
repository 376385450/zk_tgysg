package com.sinohealth.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Huawei OBS 配置
 *
 * @author wl
 * @version v1.0
 * @date 2021/7/19
 */
@Component
@ConfigurationProperties(prefix = "huawei.obs")
public class HuaweiConfig {

    /**
     * Huawei OBS 账号配置
     */
    private ObsRegistryVo registry;

    /**
     * 存储空间列表
     */
    private List<String> storageNameList;

    /**
     * 目录列表
     */
    private List<String> pathList;

    /**
     * 有效期列表
     */
    private List<Long> expirationList;

    /**
     * 缩略图配置
     */
    private Image image;

    /**
     * 单链接限速，单位：bit/s，默认5MB/s
     */
    private int trafficLimit = 1024 * 8 * 1000 * 5;

    /**
     * 公网 endpoint
     */
    private String publicEndpoint;

    /**
     * 内网 endpoint
     */
    private String privateEndpoint;


    public ObsRegistryVo getRegistry() {
        return registry;
    }

    public void setRegistry(ObsRegistryVo registry) {
        this.registry = registry;
    }

    public List<String> getStorageNameList() {
        return storageNameList;
    }

    public void setStorageNameList(List<String> storageNameList) {
        this.storageNameList = storageNameList;
    }

    public List<String> getPathList() {
        return pathList;
    }

    public void setPathList(List<String> pathList) {
        this.pathList = pathList;
    }

    public List<Long> getExpirationList() {
        return expirationList;
    }

    public void setExpirationList(List<Long> expirationList) {
        this.expirationList = expirationList;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public int getTrafficLimit() {
        return trafficLimit;
    }

    public void setTrafficLimit(int trafficLimit) {
        this.trafficLimit = trafficLimit;
    }

    public String getPublicEndpoint() {
        return publicEndpoint;
    }

    public void setPublicEndpoint(String publicEndpoint) {
        this.publicEndpoint = publicEndpoint;
    }

    public String getPrivateEndpoint() {
        return privateEndpoint;
    }

    public void setPrivateEndpoint(String privateEndpoint) {
        this.privateEndpoint = privateEndpoint;
    }

    /**
     * 图片处理配置
     */
    public static class Image {

        /**
         * 指定缩略的模式：
         * lfit：等比缩放，限制在指定w与h的矩形内的最大图片。
         * mfit：等比缩放，延伸出指定w与h的矩形框外的最小图片。
         * fill：固定宽高，将延伸出指定w与h的矩形框外的最小图片进行居中裁剪。
         * pad：固定宽高，缩略填充。
         * fixed：固定宽高，强制缩略。
         */
        private String model;

        /**
         * 指定目标缩略图的宽度。
         * 取值范围：1-4096
         */
        private String width;

        /**
         * 指定目标缩略图的高度。
         * 取值范围：1-4096
         */
        private String height;


        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }
    }

    /**
     * 注册配置
     */
    public static class ObsRegistryVo {

        /**
         * 华为云obs注册类型
         */
        private String obsType;
        /**
         * 华为云obs节点
         */
        private String endpoint;
        /**
         * 华为云obs accessKeyId
         */
        private String accessKeyId;
        /**
         * 华为云obs accessKeySecret
         */
        private String accessKeySecret;
        private String subAccessKeyId;
        private String subAccessKeySecret;
        /**
         * 桶名称
         */
        private String bucketName ;
        private String roleArn;
        /**
         * 临时访问acl
         */
        private String acl ;
        /**
         * dir
         */
        private String dir;

        public String getObsType() {
            return obsType;
        }

        public void setObsType(String obsType) {
            this.obsType = obsType;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getAccessKeySecret() {
            return accessKeySecret;
        }

        public void setAccessKeySecret(String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
        }

        public String getSubAccessKeyId() {
            return subAccessKeyId;
        }

        public void setSubAccessKeyId(String subAccessKeyId) {
            this.subAccessKeyId = subAccessKeyId;
        }

        public String getSubAccessKeySecret() {
            return subAccessKeySecret;
        }

        public void setSubAccessKeySecret(String subAccessKeySecret) {
            this.subAccessKeySecret = subAccessKeySecret;
        }

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getRoleArn() {
            return roleArn;
        }

        public void setRoleArn(String roleArn) {
            this.roleArn = roleArn;
        }

        public String getAcl() {
            return acl;
        }

        public void setAcl(String acl) {
            this.acl = acl;
        }

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }
    }

}
