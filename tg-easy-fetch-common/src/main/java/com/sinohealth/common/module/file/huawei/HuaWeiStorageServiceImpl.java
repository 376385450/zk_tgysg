package com.sinohealth.common.module.file.huawei;

import com.alibaba.fastjson.JSON;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.config.HuaweiConfig;
import com.sinohealth.common.module.file.BaseStorageService;
import com.sinohealth.common.module.file.constant.FileConst;
import com.sinohealth.common.module.file.dto.BasePath;
import com.sinohealth.common.module.file.dto.HuaweiPath;
import com.sinohealth.common.module.file.dto.ThumbConfig;
import com.sinohealth.common.module.file.dto.UploadFileDTO;
import com.sinohealth.common.utils.CompressionUtil;
import com.sinohealth.common.utils.ObsUtil;
import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * OBS 文件存储服务接口实现类
 *
 * @author wl
 * @version v1.0
 * @date 2021/07/19
 */
@Service(value = "huaweiStorageService")
public class HuaWeiStorageServiceImpl implements BaseStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HuaWeiStorageServiceImpl.class);

    @Resource
    private HuaweiConfig huaweiConfig;


    /**
     * 上传单个文件，存储到指定的存储空间
     *
     * @param storageName bucketName，Huawei OBS 的存储空间名称
     * @param inputStream 上传文件流
     * @param path        文件存储自定义目录
     * @param fileName    文件名称，必须唯一，否则会覆盖同名文件
     * @param metaData    文件元数据
     * @param expiration  链接有效期
     * @return 文件存储路径
     */

    public BasePath upload(String storageName, InputStream inputStream, String path, String fileName,
                           Map<String, String> metaData, Long expiration) {
        if (StringUtils.isBlank(storageName)) {
            // 默认存储空间
            storageName = huaweiConfig.getStorageNameList().get(0);
        }

        // 文件大小
        long fileSize;
        try {
            fileSize = inputStream.available();

        } catch (IOException e) {
            LOGGER.error("upload >> get fileSize error");
            throw new CustomException("upload >> get fileSize error");
        }
        LOGGER.info("upload >> fileSize = {}", fileSize);

        // 默认存储路径
        if (StringUtils.isBlank(path)) {
            path = huaweiConfig.getPathList().get(0);
        }

        // 文件存储路径（路径加入随机数，防止性能问题，具体说明见下面链接）
        // https://help.aliyun.com/document_detail/64945.html?spm=a2c4g.11186623.6.1548.24117c57XRgGxD
        String objectName = buildObjectName(path, fileName);
        // Huawei OBS 客户端
        PutObjectRequest request = new PutObjectRequest(storageName, objectName, inputStream);
        ObjectMetadata metadata = new ObjectMetadata();
        if (metaData != null && metaData.size() > 0) {
            for (Map.Entry<String, String> entry : metaData.entrySet()) {
                metadata.addUserMetadata(entry.getKey(), entry.getValue());
                //防止文件名元数据中含有特殊字符,传到华为云xml解析错误报错 先转义
                if (FileConst.MetaData.FILENAME.equals(entry.getKey())) {
                    metadata.addUserMetadata(entry.getKey(), entry.getValue() == null ? "" : URLEncoder.encode(entry.getValue()));
                }
            }
            request.setMetadata(metadata);
        }

        // 单链接限速
        /*if (huaweiConfig.getTrafficLimit() > 0) {
            request.set(huaweiConfig.getTrafficLimit());
        }*/
        ObsClient obsClient = ObsUtil.getObsClient();
        try {
            final PutObjectResult putObjectResult = putObject(obsClient, request);
            final String eTag = putObjectResult.getEtag();
            LOGGER.info("upload >> eTag = {}", eTag);
        } catch (Exception e) {
            LOGGER.error("upload >> 异常 {}{}", e.getMessage(), e);
            throw new RuntimeException("upload >> 异常");
        }
        // 链接有效期
        if (expiration == null) {
            expiration = huaweiConfig.getExpirationList().get(0);
        }
        final Date urlExpiryDate = new Date(new Date().getTime() + expiration);
        // 生成链接
        String url = getUrl(obsClient, storageName, objectName, null, expiration);
        String fileUrl = url.replace(huaweiConfig.getPrivateEndpoint(), huaweiConfig.getPublicEndpoint());
        return new HuaweiPath.Builder()
                .withSourcePath(objectName)
                .withStorageName(storageName)
                .withUrl(fileUrl)
                .withFileName(fileName)
                .withFileSize(fileSize)
                .withUrlExpiryDate(urlExpiryDate)
                .withUrlExpire(expiration)
                .build();
    }

    /**
     * 上传单个文件，存储到指定的存储空间
     *
     * @param type        文件类型
     * @param inputStream 上传文件流
     * @param fileName    上传文件名称
     * @param metaData    上传文件元数据
     * @return 文件存储路径
     */

    public BasePath upload(Integer type, InputStream inputStream, String fileName, Map<String, String> metaData) {
        // 存储空间名称
        String storageName;
        String path;
        Long expiration;
        if (type != null) {
            storageName = huaweiConfig.getStorageNameList().get(type);
            path = huaweiConfig.getPathList().get(type);
            expiration = huaweiConfig.getExpirationList().get(type);

        } else {
            storageName = huaweiConfig.getStorageNameList().get(0);
            path = huaweiConfig.getPathList().get(0);
            expiration = huaweiConfig.getExpirationList().get(0);
        }
        return upload(storageName, inputStream, path, fileName, metaData, expiration);
    }

    /**
     * 修改文件
     *
     * @param storageName bucketName，Huawei OBS 的存储空间名称
     * @param inputStream 上传文件流
     * @param fileName    上传文件名称
     * @param sourcePath  文件存储路径
     * @param metaData    文件元数据
     * @return 文件存储路径
     */

    public BasePath modify(String storageName, InputStream inputStream, String fileName, String sourcePath,
                           Map<String, String> metaData) {
        if (StringUtils.isBlank(storageName)) {
            // 默认存储空间
            storageName = huaweiConfig.getStorageNameList().get(0);
        }

        // 判断一下上次文件类型必须和替换文件类型一致
        String fileExtName = fileName.substring(fileName.lastIndexOf('.'));
        String oldFileExtName = sourcePath.substring(sourcePath.lastIndexOf('.'));
        if (!fileExtName.equals(oldFileExtName)) {
            LOGGER.error("modify >> The extension of the modified file is different，[{}，{}]",
                    fileExtName, oldFileExtName);
            throw new CustomException("modify >> The extension of the modified file is different，[{}，{}]");
        }
        //文件是否确定存在
        if (!existsObject(storageName, sourcePath)) {
            LOGGER.warn("modify >> modify file is not found，storageName[{}]，sourcePath[{}]",
                    storageName, sourcePath);
            return null;
        }
        // Huawei OBS 客户端
        PutObjectRequest request = new PutObjectRequest(storageName, sourcePath, inputStream);
        // 单链接限速
        /*if (huaweiConfig.getTrafficLimit() > 0) {
            request.setTrafficLimit(huaweiConfig.getTrafficLimit());
        }*/
        ObsClient obsClient = ObsUtil.getObsClient();
        // 上传文件
        final PutObjectResult putObjectResult = putObject(obsClient, request);
        final String eTag = putObjectResult.getEtag();
        LOGGER.info("modify >> ETag = {}", eTag);
        // 获取文件访问链接
        String url = getUrl(obsClient, storageName, sourcePath, null, null);
        String fileUrl = url.replace(huaweiConfig.getPrivateEndpoint(), huaweiConfig.getPublicEndpoint());

        return new HuaweiPath.Builder()
                .withSourcePath(sourcePath)
                .withStorageName(storageName)
                .withUrl(fileUrl)
                .build();
    }

    /**
     * 修改文件
     *
     * @param type        文件类型
     * @param inputStream 上传文件流
     * @param fileName    上传文件名称
     * @param sourcePath  替换文件存储路径
     * @param metaData    上传文件元数据
     * @return 文件存储路径
     */

    public BasePath modify(Integer type, InputStream inputStream, String fileName, String sourcePath,
                           Map<String, String> metaData) {
        // 存储空间名称
        String storageName;
        if (type != null) {
            storageName = huaweiConfig.getStorageNameList().get(type);

        } else {
            storageName = huaweiConfig.getStorageNameList().get(0);
        }

        return modify(storageName, inputStream, fileName, sourcePath, metaData);
    }

    /**
     * 删除单个文件
     *
     * @param storageName bucketName
     * @param sourcePath  文件存储路径
     * @return 删除结果，true 删除成功，false 删除失败
     */

    public boolean delete(String storageName, String sourcePath) {

        if (StringUtils.isBlank(storageName)) {
            // 默认存储空间
            storageName = huaweiConfig.getStorageNameList().get(0);
        }
        //文件是否确定存在
        if (!existsObject(storageName, sourcePath)) {
            LOGGER.warn("delete >> delete file is not found，storageName[{}]，sourcePath[{}]",
                    storageName, sourcePath);
            throw new CustomException("delete >> delete file is not found，storageName[{}]，sourcePath[{}]");
        }
        try {
            ObsClient obsClient = ObsUtil.getObsClient();
            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(storageName, sourcePath);
            LOGGER.info("删除参数 deleteObjectRequest：{}", JSON.toJSONString(deleteObjectRequest));
            DeleteObjectResult deleteObjectResult = obsClient.deleteObject(deleteObjectRequest);
            LOGGER.info("删除结果 deleteObjectResult：{}", JSON.toJSONString(deleteObjectResult));
            return true;
        } catch (Exception e) {
            LOGGER.error("delete >> delete object error", e);
        }
        return true;
    }

    /**
     * 删除单个文件
     *
     * @param type       文件类型
     * @param sourcePath 文件存储路径
     * @return 删除结果，true 删除成功，false 删除失败
     */

    public boolean delete(Integer type, String sourcePath) {
        // 存储空间名称
        String storageName;
        if (type != null) {
            storageName = huaweiConfig.getStorageNameList().get(type);

        } else {
            storageName = huaweiConfig.getStorageNameList().get(0);
        }
        return delete(storageName, sourcePath);
    }

    /**
     * 下载单个文件
     *
     * @param storageName 存储空间名称
     * @param sourcePath  文件存储路径
     * @return 文件字节数组
     */

    public byte[] downloadWithBytes(String storageName, String sourcePath) {
        if (StringUtils.isBlank(storageName)) {
            // 默认存储空间
            storageName = huaweiConfig.getStorageNameList().get(0);
        }
        LOGGER.info("downloadWithBytes >> 下载单个文件，storageName[{}]，sourcePath[{}]", storageName, sourcePath);

        // 先确定文件是否存在
        if (!existsObject(storageName, sourcePath)) {
            LOGGER.warn("downloadWithBytes >> download file is not found，storageName[{}]，sourcePath[{}]",
                    storageName, sourcePath);
            throw new CustomException("downloadWithBytes >> download file is not found，storageName[{}]，sourcePath[{}]");
        }

        // Huawei OBS 客户端
        GetObjectRequest request = new GetObjectRequest(storageName, sourcePath);
        // 单链接限速
        /*if (huaweiConfig.getTrafficLimit() > 0) {
            request.setTrafficLimit(huaweiConfig.getTrafficLimit());
        }*/

        // 获取文件
        ObsClient obsClient = ObsUtil.getObsClient();
        ObsObject obsObject = obsClient.getObject(request);
        final InputStream objectContent = obsObject.getObjectContent();
        if (objectContent == null) {
            LOGGER.warn("downloadWithBytes >> download file null，storageName[{}]，sourcePath[{}]",
                    storageName, sourcePath);
            throw new CustomException("downloadWithBytes >> download file null，storageName[{}]，sourcePath[{}]");
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];

            int bytesRead;
            while ((bytesRead = objectContent.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new ObsException("CannotReadContentStream", e);
        } finally {
            try {
                objectContent.close();
            } catch (IOException e) {
                LOGGER.error("downloadWithBytes >>  异常", e);
            }
        }
    }

    /**
     * 下载单个文件
     *
     * @param type       文件类型
     * @param sourcePath 文件存储路径
     * @return 文件字节数组
     */

    public byte[] downloadWithBytes(Integer type, String sourcePath) {
        // 存储空间名称
        String storageName;
        if (type != null) {
            storageName = huaweiConfig.getStorageNameList().get(type);
        } else {
            storageName = huaweiConfig.getStorageNameList().get(0);
        }

        return downloadWithBytes(storageName, sourcePath);
    }

    /**
     * 下载单个文件（需要显式关闭流）
     *
     * @param storageName 存储空间名称
     * @param sourcePath  文件存储路径
     * @return 文件流
     */

    public InputStream download(String storageName, String sourcePath) {
        if (StringUtils.isBlank(storageName)) {
            // 默认存储空间
            storageName = huaweiConfig.getStorageNameList().get(0);
        }
        // 先确定文件是否存在
        if (!existsObject(storageName, sourcePath)) {
            LOGGER.warn("download >> download file is not found，storageName[{}]，sourcePath[{}]",
                    storageName, sourcePath);
            throw new CustomException("download >> download file is not found，storageName[{}]，sourcePath[{}]");
        }

        // Huawei OBS 客户端
        GetObjectRequest request = new GetObjectRequest(storageName, sourcePath);
        // 单链接限速
        /*if (huaweiConfig.getTrafficLimit() > 0) {
            request.setTrafficLimit(huaweiConfig.getTrafficLimit());
        }*/
        InputStream objectContent = null;
        try {
            // 获取文件
            ObsClient obsClient = ObsUtil.getObsClient();
            ObsObject obsObject = obsClient.getObject(request);
            objectContent = obsObject.getObjectContent();
            if (objectContent == null) {
                LOGGER.warn("download >> download file null，storageName[{}]，sourcePath[{}]",
                        storageName, sourcePath);
                throw new CustomException("download >> download file null，storageName[{}]，sourcePath[{}]");
            }
            return objectContent;
        } catch (Exception e) {
            LOGGER.error("文件 download 异常", e);
            throw new RuntimeException("文件 download 异常");
        } finally {
            try {
                if (objectContent != null) {
                    objectContent.close();
                }
            } catch (IOException e) {
                LOGGER.error("objectContent.close() 异常", e);
            }
        }
    }

    /**
     * 下载单个文件（需要显式关闭流）
     *
     * @param type       文件类型
     * @param sourcePath 文件存储路径
     * @return 文件流
     */

    public InputStream download(Integer type, String sourcePath) {
        // 存储空间名称
        String storageName;
        if (type != null) {
            storageName = huaweiConfig.getStorageNameList().get(type);

        } else {
            storageName = huaweiConfig.getStorageNameList().get(0);
        }

        return download(storageName, sourcePath);
    }


    /**
     * 批量上传文件
     *
     * @param storageName 存储空间名称
     * @param fileList    上传文件对象列表
     * @return 上传文件存储路径和访问链接列表
     */

    public List<BasePath> uploadBatch(String storageName, List<UploadFileDTO> fileList) {
        if (StringUtils.isBlank(storageName)) {
            // 默认存储空间
            storageName = huaweiConfig.getStorageNameList().get(0);
        }

        List<BasePath> pathList = new ArrayList<>();
        for (UploadFileDTO file : fileList) {
            HuaweiPath aliyunPath = (HuaweiPath) upload(storageName, file.getInputStream(), file.getPath(),
                    file.getFileName(), file.getMetaData(), null);

            pathList.add(aliyunPath);
        }
        return pathList;
    }

    /**
     * 批量上传文件
     *
     * @param type     文件类型
     * @param fileList 上传文件对象列表
     * @return 上传文件存储路径和访问链接列表
     */

    public List<BasePath> uploadBatch(Integer type, List<UploadFileDTO> fileList) {
        // 存储空间名称
        String storageName;
        String path;
        if (type != null) {
            storageName = huaweiConfig.getStorageNameList().get(type);
            path = huaweiConfig.getPathList().get(type);

        } else {
            storageName = huaweiConfig.getStorageNameList().get(0);
            path = huaweiConfig.getPathList().get(0);
        }

        fileList.forEach(uploadFileDTO -> {
            uploadFileDTO.setPath(path);
        });

        return uploadBatch(storageName, fileList);
    }

    /**
     * 批量删除文件
     *
     * @param storageName    存储空间名称
     * @param sourcePathList 文件存储路径列表
     * @return 删除结果，返回删除成功的文件存储路径
     */

    public List<String> deleteBatch(String storageName, List<String> sourcePathList) {
        if (StringUtils.isBlank(storageName)) {
            // 默认存储空间
            storageName = huaweiConfig.getStorageNameList().get(0);
        }
        // 批量判断文件是否存在，存在才删除
        List<String> existList = new ArrayList<>();
        for (String path : sourcePathList) {
            if (existsObject(storageName, path)) {
                existList.add(path);
            }
        }
        // 删除文件都不存在
        if (existList.isEmpty()) {
            return existList;
        }
        List<String> correctPaths = new ArrayList<>();
        try {
            // 构造批量删除文件请求
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(storageName);
            deleteObjectsRequest.setQuiet(false);
            List<KeyAndVersion> toDelete = new ArrayList<>();
            for (String path : existList) {
                toDelete.add(new KeyAndVersion(path));
            }
            deleteObjectsRequest.setKeyAndVersions(toDelete.toArray(new KeyAndVersion[toDelete.size()]));
            ObsClient obsClient = ObsUtil.getObsClient();
            DeleteObjectsResult deleteObjectsResult = obsClient.deleteObjects(deleteObjectsRequest);
            //成功删除文件对象
            List<DeleteObjectsResult.DeleteObjectResult> deletedObjectResults = deleteObjectsResult.getDeletedObjectResults();
            if (!ObjectUtils.isEmpty(deletedObjectResults)) {
                // 返回成功删除文件列表
                correctPaths = deletedObjectResults.stream().map(DeleteObjectsResult.DeleteObjectResult::getObjectKey).collect(Collectors.toList());
            }
        } catch (Exception e) {
            LOGGER.error("文件deleteBatch download 异常{}{}", e.getMessage(), e);
            throw new RuntimeException("文件deleteBatch download 异常");
        }
        return correctPaths;
    }

    /**
     * 批量删除文件
     *
     * @param type           文件类型
     * @param sourcePathList 文件存储路径列表
     * @return 删除结果，返回删除成功的文件存储路径
     */

    public List<String> deleteBatch(Integer type, List<String> sourcePathList) {
        // 存储空间名称
        String storageName;
        String pathList;
        if (type != null) {
            storageName = huaweiConfig.getStorageNameList().get(type);
            pathList = huaweiConfig.getPathList().get(type);

        } else {
            storageName = huaweiConfig.getStorageNameList().get(0);
            pathList = huaweiConfig.getPathList().get(0);

        }
        return deleteBatch(storageName, sourcePathList.stream().map(m -> buildObjectName(pathList, m)).collect(Collectors.toList()));
    }

    /**
     * 批量下载文件
     *
     * @param storageName    存储空间名称
     * @param sourcePathList 文件存储路径列表
     * @return 下载结果，返回下载成功的文件流
     */

    public byte[] downloadBatch(String storageName, List<String> sourcePathList) {
        if (StringUtils.isBlank(storageName)) {
            // 默认存储空间
            storageName = huaweiConfig.getStorageNameList().get(0);
        }
        List<CompressionUtil.ZipFileInfo> fileInfoList = new ArrayList<>();
        for (String sourcePath : sourcePathList) {
            CompressionUtil.ZipFileInfo fileInfo = new CompressionUtil.ZipFileInfo();

            // 单个文件下载
            fileInfo.setBytes(downloadWithBytes(storageName, sourcePath));
            // 文件元信息
            final Map<String, String> userMetadata = getMetadata(storageName, sourcePath);
            fileInfo.setName(getFileNameFromObjectName(userMetadata.get(FileConst.MetaData.OBJECT_KEY)));
            fileInfoList.add(fileInfo);
        }
        try {
            return CompressionUtil.compressByZip(fileInfoList);

        } catch (IOException e) {
            LOGGER.error("downloadBatch >> file compression fail", e);
            throw new CustomException("downloadBatch >> file compression fail");
        }
    }

    /**
     * 批量下载文件
     *
     * @param type           文件类型
     * @param sourcePathList 文件存储路径列表
     * @return 下载结果，返回下载成功的文件流
     */

    public byte[] downloadBatch(Integer type, List<String> sourcePathList) {
        // 存储空间名称
        String storageName;
        if (type != null) {
            storageName = huaweiConfig.getStorageNameList().get(type);

        } else {
            storageName = huaweiConfig.getStorageNameList().get(0);
        }
        return downloadBatch(storageName, sourcePathList);
    }

    /**
     * 上传单张图片，生成缩略图链接
     * <p>
     * Huawei OBS 缩略图是通过原图片链接拼接参数的方式访问缩略图
     *
     * @param storageName 存储空间名称
     * @param inputStream 上传文件流
     * @param path        上传文件存储路径
     * @param fileName    上传文件名称
     * @param metaData    上传文件元数据
     * @param expiration  链接有效期
     * @return 上传图片存储路径和访问链接，以及缩略图的存储路径
     */

    public BasePath uploadImage(String storageName, InputStream inputStream, String path, String fileName,
                                Map<String, String> metaData, ThumbConfig thumbConfig, Long expiration) {
        if (StringUtils.isBlank(storageName)) {
            // 默认存储空间
            storageName = huaweiConfig.getStorageNameList().get(0);
        }
        // 链接有效期
        if (expiration == null) {
            expiration = huaweiConfig.getExpirationList().get(0);
        }
        // 上传文件
        HuaweiPath huaweiPath = (HuaweiPath) upload(storageName, inputStream, path, fileName, metaData, expiration);
        // 图片缩略图配置
        String styleStr;
        try (Formatter style = new Formatter()) {
            if (thumbConfig != null) {
                // 自定义配置
                Double percent = thumbConfig.getPercent();
                style.format("%s", "image/resize");
                if (percent != null) {
                    if (percent < 0 || percent > 10) {
                        LOGGER.warn("uploadImage >> thumbConfig percent invalid");
                        throw new CustomException("uploadImage >> thumbConfig percent invalid");
                    }
                    style.format(",p_%s", (int) (percent * 100));
                } else {
                    if (StringUtils.isNotBlank(thumbConfig.getModel())) {
                        style.format(",m_%s", thumbConfig.getModel());
                    }
                    if (thumbConfig.getWidth() != null) {
                        style.format(",w_%s", thumbConfig.getWidth());
                    }
                    if (thumbConfig.getHeight() != null) {
                        style.format(",h_%s", thumbConfig.getHeight());
                    }
                }
            } else {
                // 默认配置
                HuaweiConfig.Image image = huaweiConfig.getImage();
                style.format("image/resize,m_%s,w_%s,h_%s", image.getModel(), image.getWidth(), image.getHeight());
            }
            styleStr = style.toString();
        }
        // 获取图片缩略图
        ObsClient obsClient = ObsUtil.getObsClient();
        String url = getZoomPicUrl(obsClient, storageName, huaweiPath.getSourcePath(), styleStr, expiration);
        String thumbUrl = url.replace(huaweiConfig.getPrivateEndpoint(), huaweiConfig.getPublicEndpoint());
        huaweiPath.setThumbUrl(thumbUrl);
        // 有效期
        huaweiPath.setUrlExpiryDate(new Date(new Date().getTime() + expiration));
        return huaweiPath;
    }

    /**
     * 上传图片，并生成缩略图
     *
     * @param type        文件类型
     * @param inputStream 上传文件流
     * @param fileName    上传文件名称
     * @param metaData    上传文件元数据
     * @param thumbConfig 缩略图配置
     * @return 上传图片存储路径和缩略图的存储路径
     */

    public BasePath uploadImage(Integer type, InputStream inputStream, String fileName, Map<String, String> metaData,
                                ThumbConfig thumbConfig, Long expiration) {
        // 存储空间名称
        String storageName;
        String path;
        if (type != null) {
            storageName = huaweiConfig.getStorageNameList().get(type);
            path = huaweiConfig.getPathList().get(type);
            if (expiration == null) {
                expiration = huaweiConfig.getExpirationList().get(type);
            }
        } else {
            storageName = huaweiConfig.getStorageNameList().get(0);
            path = huaweiConfig.getPathList().get(0);
            if (expiration == null) {
                expiration = huaweiConfig.getExpirationList().get(0);
            }
        }
        return uploadImage(storageName, inputStream, path, fileName, metaData, thumbConfig, expiration);
    }

    /**
     * 获取文件访问链接
     *
     * @param obsClient        Huawei OBS 客户端
     * @param storageName      存储空间名称
     * @param sourcePath       文件存储路径
     * @param specialParamEnum 文件处理类型
     * @param expiration       链接有效期，单位毫秒
     * @return 文件临时访问链接
     */
    private String getUrl(ObsClient obsClient, String storageName, String sourcePath, SpecialParamEnum specialParamEnum, Long expiration) {
//        // 设置过期时间
//        Date expirationDate;
//        if (expiration != null) {
//            expirationDate = new Date(new Date().getTime() + expiration);
//        } else {
//            expirationDate = new Date(new Date().getTime() + huaweiConfig.getExpirationList().get(0));
//        }
        TemporarySignatureRequest request = new TemporarySignatureRequest();
        request.setMethod(HttpMethodEnum.GET);
//        request.setRequestDate(expirationDate);
        request.setBucketName(storageName);
        request.setObjectKey(sourcePath);
        request.setSpecialParam(specialParamEnum);
        request.setExpires(expiration);
        // 生成临时访问链接
        String signUrl = "";
        try {
            TemporarySignatureResponse temporarySignature = obsClient.createTemporarySignature(request);
            signUrl = temporarySignature.getSignedUrl();
        } catch (ObsException e) {
            LOGGER.error("获取文件访问链接====异常", e);
            throw new RuntimeException("获取文件访问链接异常");
        }
        return signUrl;
    }

    /**
     * 获取图片缩略图访问链接
     *
     * @param obsClient   Huawei OBS 客户端
     * @param storageName 存储空间名称
     * @param sourcePath  文件存储路径
     * @param picStyleStr 图片处理参数
     * @param expiration  链接有效期，单位毫秒
     * @return 文件临时访问链接
     */
    private String getZoomPicUrl(ObsClient obsClient, String storageName, String sourcePath, String picStyleStr, Long expiration) {
        // 设置过期时间
        Date expirationDate;
        if (expiration != null) {
            expirationDate = new Date(new Date().getTime() + expiration);
        } else {
            expirationDate = new Date(new Date().getTime() + huaweiConfig.getExpirationList().get(0));
        }
        TemporarySignatureRequest request = new TemporarySignatureRequest();
        request.setMethod(HttpMethodEnum.GET);
        request.setRequestDate(expirationDate);
        request.setBucketName(storageName);
        request.setObjectKey(sourcePath);
        // 设置图片处理参数，例如对图片依次进行缩放、旋转
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("x-image-process", picStyleStr);
        request.setQueryParams(queryParams);
        String signUrl = "";
        try {
            // 生成临时访问链接
            TemporarySignatureResponse temporarySignature = obsClient.createTemporarySignature(request);
            signUrl = temporarySignature.getSignedUrl();
        } catch (ObsException e) {
            LOGGER.error("获取图片缩略图访问链接====异常", e);
            throw new RuntimeException("获取图片缩略图访问链接异常");
        }
        return signUrl;
    }

    /**
     * 获取文件元信息
     *
     * @param storageName 存储空间名称
     * @param sourcePath  文件存储路径
     * @return 文件元信息
     */

    public Map<String, String> getMetadata(String storageName, String sourcePath) {
        if (StringUtils.isBlank(storageName)) {
            // 默认存储空间
            storageName = huaweiConfig.getStorageNameList().get(0);
        }
        GetObjectRequest request = new GetObjectRequest(storageName, sourcePath);
        ObsObject obsObject = null;
        Map<String, String> resultMap = new HashMap<>();
        try {
            ObsClient obsClient = ObsUtil.getObsClient();
            obsObject = obsClient.getObject(request);
            resultMap.put(FileConst.MetaData.OBJECT_KEY, obsObject.getObjectKey());
            final ObjectMetadata objectMetadata = obsObject.getMetadata();
            Map<String, Object> objectMap = objectMetadata.getMetadata();
            if (objectMap != null && objectMap.size() > 0) {
                for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
                    resultMap.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue().toString());
                }
            }
            return resultMap;
        } catch (Exception e) {
            LOGGER.error("getMetadata 异常", e);
            throw new RuntimeException("获取文件元信息异常");
        }
    }

    /**
     * 获取文件元信息
     *
     * @param type       文件类型
     * @param sourcePath 文件存储路径
     * @return 文件元信息
     */

    public Map<String, String> getMetadata(Integer type, String sourcePath) {
        // 存储空间名称
        String storageName;
        if (type != null) {
            storageName = huaweiConfig.getStorageNameList().get(type);
        } else {
            storageName = huaweiConfig.getStorageNameList().get(0);
        }
        return getMetadata(storageName, sourcePath);
    }

    /**
     * 上传文件到临时目录
     *
     * @param fileDTO 上传文件
     * @return 上传文件存储路径和访问链接
     * @author linkaiwei
     * @date 2020-09-07 15:34:52
     * @since 1.5.6
     */

    public BasePath uploadTemp(UploadFileDTO fileDTO) {
        // 存储空间名称
        String storageName = huaweiConfig.getStorageNameList().get(FileConst.Type.TEMP.getType());
        // 文件存储目录
        String path = huaweiConfig.getPathList().get(FileConst.Type.TEMP.getType());
        // 上传文件
        return upload(storageName, fileDTO.getInputStream(), path, fileDTO.getFileName(),
                fileDTO.getMetaData(), null);
    }

    /**
     * 复制文件（从临时目录到正式目录）
     *
     * @param copyFilePath 要复制的文件存储路径
     * @param type         文件类型，详情见{@link FileConst.Type}
     * @return 新复制文件存储路径
     * @author wl
     * @date 2021-07-20
     */

    public BasePath copyFile(String copyFilePath, Integer type) {
        // 存储空间名称
        String storageName;
        String path;
        if (type != null) {
            storageName = huaweiConfig.getStorageNameList().get(type);
            path = huaweiConfig.getPathList().get(type);

        } else {
            storageName = huaweiConfig.getStorageNameList().get(0);
            path = huaweiConfig.getPathList().get(0);
        }
        // 文件原名称（包含后缀）
        final String fileName = copyFilePath.substring(copyFilePath.lastIndexOf("/") + 1);
        // 新复制文件存储路径
        String objectName = buildObjectName(path, fileName);

        // 临时存储空间名称
        String tempStorageName = huaweiConfig.getStorageNameList().get(FileConst.Type.TEMP.getType());

        // Huawei OBS 客户端
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(tempStorageName, copyFilePath, storageName, objectName);
        ObsClient obsClient = ObsUtil.getObsClient();
        final CopyObjectResult result = obsClient.copyObject(copyObjectRequest);
        LOGGER.info("copyFile >> 复制文件结果，eTag = [{}]", result.getEtag());
        return new HuaweiPath.Builder()
                .withSourcePath(objectName)
                .withStorageName(storageName)
                .withFileName(fileName)
                .build();
    }

    @Override
    public List<String> getUrlBatch(Integer type, List<String> sourcePathList) {
        // 存储空间名称
        String storageName;
        String pathList;
        Long expiration;
        if (type != null) {
            storageName = huaweiConfig.getStorageNameList().get(type);
            pathList = huaweiConfig.getPathList().get(type);
            expiration = huaweiConfig.getExpirationList().get(type);

        } else {
            storageName = huaweiConfig.getStorageNameList().get(0);
            pathList = huaweiConfig.getPathList().get(0);
            expiration = huaweiConfig.getExpirationList().get(0);

        }
        ObsClient obsClient = ObsUtil.getObsClient();

        return sourcePathList.stream()
                .distinct()
                .map(m -> getUrl(obsClient, storageName, buildObjectName(pathList, m), null, expiration))
                .collect(Collectors.toList());
    }

    /**
     * 构造文件存储路径
     *
     * @param path     文件目录
     * @param fileName 文件原名称（包含后缀）
     * @return 文件存储路径
     * @author linkaiwei
     * @date 2020-09-07 16:48:03
     * @since 1.5.6
     */
    private String buildObjectName(String path, String fileName) {
        // 文件存储路径（路径加入随机数，防止性能问题，具体说明见下面链接）
        // https://help.aliyun.com/document_detail/64945.html?spm=a2c4g.11186623.6.1548.24117c57XRgGxD
//        String objectName = path + "/" + DateUtils.today() + "/" + UuidUtil.generate(4) + "/" + fileName;
        String objectName = path + "/" + fileName;
        return objectName.replace("//", "/");
    }

    /**
     * 对象名称获取文件名
     *
     * @param path
     * @return
     */
    private static String getFileNameFromObjectName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    /**
     * @param bucketName 桶名称
     * @param sourcePath 文件路径
     * @return
     */
    private boolean existsObject(String bucketName, String sourcePath) {
        try {
            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, sourcePath);
            ObsClient obsClient = ObsUtil.getObsClient();
            ObsObject getObject = obsClient.getObject(getObjectRequest);
            //文件是否确定存在
            if (!bucketName.equals(getObject.getBucketName()) || !sourcePath.equals(getObject.getObjectKey())) {
                LOGGER.warn("modify >> modify file is not found，storageName[{}]，sourcePath[{}]",
                        bucketName, sourcePath);
                return false;
            }
            return true;
        } catch (ObsException e) {
            if ("NoSuchKey".equals(e.getErrorCode()) || e.getResponseCode() == 404) {
                LOGGER.error("======没有该文件===========", e);
            } else {
                LOGGER.error("判断文件对象是否存在异常" + e.getErrorMessage(), e);
            }
        }
        return false;
    }

    /**
     * 上传对象
     *
     * @param obsClient
     * @param putObjectRequest
     * @return
     */
    private PutObjectResult putObject(ObsClient obsClient, PutObjectRequest putObjectRequest) {
        PutObjectResult putObjectResult = null;
        try {
            putObjectResult = obsClient.putObject(putObjectRequest);
        } catch (Exception e) {
            LOGGER.error("obs putObject >>异常 buckentName {} objectKey {} 异常信息 {} 异常:{}", putObjectRequest.getBucketName(), putObjectRequest.getObjectKey(), e.getMessage(), e);
            throw new RuntimeException("obs putObject >>异常");
        }
        return putObjectResult;
    }



}
