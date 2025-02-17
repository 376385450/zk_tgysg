package com.sinohealth.common.module.file.constant;

/**
 * 文件服务常量
 *
 * @author linkaiwei
 * @version v1.0
 * @date 2020/4/21 11:24
 */
public class FileConst {

    private FileConst() {
    }

    /**
     * 压缩类型
     */
    public static final class CompressionType {
        private CompressionType() {
        }

        public static final String DEFLATE = "deflate";
        public static final String GZIP = "gzip";
    }

    /**
     * 压缩文件后缀名
     * 因为 FastDFS 的后缀名称只支持6位，所以截取了前6位
     */
    public static final class CompressionExtName {
        private CompressionExtName() {
        }

        public static final String DEFLATE_EXT_NAME = "deflat";
        public static final String GZIP_EXT_NAME = "gz";
    }

    /**
     * 存储类型
     */
    public static final class StorageType {
        private StorageType() {
        }

        public static final String ALIYUN_OSS = "aliyun";
        public static final String FASTDFS = "fastdfs";
        public static final String HUAWEI_OBS = "huawei";
    }

    /**
     * 文件元信息
     * 为了兼容 Aliyun OSS，元信息里面字符串全部小写，且不带特殊符号
     */
    public static final class MetaData {
        private MetaData() {
        }

        public static final String FILENAME = "filename";
        public static final String FILE_EXTNAME = "fileextname";
        public static final String CREATE_TIME = "createtime";
        public static final String MODIFY_TIME = "modifytime";
        public static final String OBJECT_KEY = "objectKey";
    }

    /**
     * FastDFS 组名，判断文件存储路径用
     */
    public static final String FASTDFS_GROUP = "group";

    /**
     * 默认0
     */
    public static enum Type {
        TEMP(0),//临时目录
        ARROGANCY(1);//字典图片
        private Integer type;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        private Type(Integer type) {
            this.type = type;
        }

        public static Type findType(Integer type) {

            for (Type value : values()) {
                if (value.getType() == type) return value;
            }
            return null;
        }

    }

}
