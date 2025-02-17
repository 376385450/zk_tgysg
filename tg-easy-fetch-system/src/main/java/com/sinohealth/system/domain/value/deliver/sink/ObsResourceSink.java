package com.sinohealth.system.domain.value.deliver.sink;/**
 * @author linshiye
 */

import com.alibaba.fastjson.JSON;
import com.sinohealth.common.module.file.dto.HuaweiPath;
import com.sinohealth.saas.file.api.rest.FileApi;
import com.sinohealth.saas.file.model.dto.request.CreateRequestDTO;
import com.sinohealth.saas.file.model.dto.response.CreateResponseDTO;
import com.sinohealth.sca.base.model.dto.response.PlatformResponse;
import com.sinohealth.system.config.FileProperties;
import com.sinohealth.system.domain.value.deliver.DeliverResourceType;
import com.sinohealth.system.domain.value.deliver.Resource;
import com.sinohealth.system.domain.value.deliver.ResourceSink;
import lombok.SneakyThrows;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author lsy
 * @version 1.0
 * @date 2023-03-10 11:26 上午
 */
public class ObsResourceSink implements ResourceSink<ObsResourceSink, HuaweiPath> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObsResourceSink.class);

    private Resource resource;

    private DeliverResourceType type;

    private FileApi fileApi;

    private FileProperties properties;

    public void setUploadHandler(FileApi fileApi){
        this.fileApi = fileApi;
    }

    public ObsResourceSink() {
    }

    public ObsResourceSink(Resource resource, DeliverResourceType type, FileApi fileApi, FileProperties properties) {
        this.resource = resource;
        this.type = type;
        this.fileApi = fileApi;
        this.properties = properties;
    }

    @Override
    public ObsResourceSink setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public ObsResourceSink setType(DeliverResourceType type) {
        this.type = type;
        return this;
    }

    @Override
    public DeliverResourceType getType() {
        return null;
    }

    @Override
    public HuaweiPath process() {
        if (resource == null) {
            return null;
        }
        return uploadFile();
    }

    @SneakyThrows
    private HuaweiPath uploadFile() {
        HuaweiPath huaweiPath = new HuaweiPath();
        CreateRequestDTO request = new CreateRequestDTO();
        request.setUploadPath("tg-easy-fetch/async");
        request.setPolicyRead(true);
        request.setEncodeFileName(false);
        request.setStorageConfigCode(properties.getFileStorageCode());
        MultipartFile multipartFile = getMultipartFile(resource.getInputStream(), resource.getName());
        PlatformResponse<CreateResponseDTO> uploadResult = fileApi.createFile(multipartFile, JSON.toJSONString(request));
        LOGGER.info("uploadResult={}", uploadResult);
        if (uploadResult.isSuccess() && uploadResult.getResult() != null){
            huaweiPath = new HuaweiPath.Builder()
                    .withSourcePath(uploadResult.getResult().getSourcePath())
                    .withUrl(uploadResult.getResult().getUrl())
                    .withFileName(uploadResult.getResult().getFileName())
                    .build();
        } else {
            LOGGER.error("异步任务上传文件失败，响应[{}]",uploadResult);
        };
        return huaweiPath;
    }

    /**
     * 转 MultipartFile
     */
    public MultipartFile getMultipartFile(InputStream inputStream, String fileName) {
        FileItem fileItem = createFileItem(inputStream, fileName);
        //CommonsMultipartFile是feign对multipartFile的封装，但是要FileItem类对象
        return new CommonsMultipartFile(fileItem);
    }

    /**
     * FileItem类对象创建
     */
    public FileItem createFileItem(InputStream inputStream, String fileName) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        String textFieldName = "file";
        FileItem item = factory.createItem(textFieldName, MediaType.MULTIPART_FORM_DATA_VALUE, true, fileName);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        OutputStream os = null;
        //使用输出流输出输入流的字节
        try {
            os = item.getOutputStream();
            while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            inputStream.close();
        } catch (IOException e) {
            LOGGER.error("Stream copy exception", e);
            throw new IllegalArgumentException("文件上传失败");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    LOGGER.error("Stream close exception", e);

                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Stream close exception", e);
                }
            }
        }

        return item;
    }

}
