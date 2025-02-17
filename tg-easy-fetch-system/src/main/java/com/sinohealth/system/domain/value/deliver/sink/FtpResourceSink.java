package com.sinohealth.system.domain.value.deliver.sink;

import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.domain.value.deliver.DeliverResourceType;
import com.sinohealth.system.domain.value.deliver.Resource;
import com.sinohealth.system.domain.value.deliver.ResourceSink;
import com.sinohealth.system.util.FtpClient;
import com.sinohealth.system.util.FtpClientFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2024-01-22 15:06
 */
@Slf4j
public class FtpResourceSink implements ResourceSink<FtpResourceSink, String> {

    private Resource resource;

    private String remote;

    @Override
    public FtpResourceSink setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public FtpResourceSink setType(DeliverResourceType type) {
        return this;
    }

    @Override
    public DeliverResourceType getType() {
        return null;
    }

    public FtpResourceSink setRemote(String remote) {
        this.remote = remote;
        return this;
    }

    /**
     * 上传文件到ftp服务器，返回ftp文件访问uri
     * @return 文件访问uri
     */
    @Override
    @SneakyThrows
    public String process() {
        if (StringUtils.isEmpty(remote)) {
            throw new IllegalArgumentException("ftp remote cannot be null");
        }
        FtpClient ftpClient = FtpClientFactory.getInstance();
        try {
            ftpClient.open();
            ftpClient.uploadFile(remote, resource.getInputStream());
        } finally {
            try {
                ftpClient.close();
            } catch (Exception e) {
                log.error("ftp关闭失败", e);
            }
            try {
                resource.close();
            } catch (Exception ignored) {

            }
        }
        return remote;
    }

}
