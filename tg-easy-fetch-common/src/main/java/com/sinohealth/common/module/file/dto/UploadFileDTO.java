package com.sinohealth.common.module.file.dto;

import java.io.InputStream;
import java.util.Map;

/**
 * 上传文件对象
 *
 * @author linkaiwei
 * @version v1.0
 * @date 2020/4/23 11:17
 */
public class UploadFileDTO {

    private InputStream inputStream;

    private byte[] bytes;

    private String path;

    private String fileName;

    private Map<String, String> metaData;

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }


    /**
     * Builder
     */
    public static final class Builder {

        private InputStream inputStream;
        private byte[] bytes;
        private String path;
        private String fileName;
        private Map<String, String> metaData;


        public Builder withInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        public Builder withBytes(byte[] bytes) {
            this.bytes = bytes;
            return this;
        }

        public Builder withPath(String path) {
            this.path = path;
            return this;
        }

        public Builder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder withMetaData(Map<String, String> metaData) {
            this.metaData = metaData;
            return this;
        }

        public UploadFileDTO build() {
            UploadFileDTO uploadFileDTO = new UploadFileDTO();
            uploadFileDTO.setInputStream(inputStream);
            uploadFileDTO.setBytes(bytes);
            uploadFileDTO.setPath(path);
            uploadFileDTO.setFileName(fileName);
            uploadFileDTO.setMetaData(metaData);
            return uploadFileDTO;
        }
    }
}
