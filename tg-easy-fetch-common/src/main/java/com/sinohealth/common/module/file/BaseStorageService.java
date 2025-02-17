package com.sinohealth.common.module.file;

import com.sinohealth.common.module.file.constant.FileConst;
import com.sinohealth.common.module.file.dto.BasePath;
import com.sinohealth.common.module.file.dto.ThumbConfig;
import com.sinohealth.common.module.file.dto.UploadFileDTO;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 文件服务接口
 *
 * @author linkaiwei
 * @version v1.0
 * @date 2020/4/21 16:04
 */
public interface BaseStorageService {

    /**
     * 上传单个文件
     *
     * @param storageName 存储空间名称
     * @param inputStream 上传文件流
     * @param path        上传文件存储路径
     * @param fileName    上传文件名称
     * @param metaData    上传文件元数据
     * @param expiration  链接有效期
     * @return 上传文件存储路径和访问链接
     */
    BasePath upload(String storageName, InputStream inputStream, String path, String fileName,
                    Map<String, String> metaData, Long expiration);

    /**
     * 上传单个文件
     *
     * @param type        文件类型
     * @param inputStream 上传文件流
     * @param fileName    上传文件名称
     * @param metaData    上传文件元数据
     * @return 上传文件存储路径和访问链接
     */
    BasePath upload(Integer type, InputStream inputStream, String fileName, Map<String, String> metaData);

    /**
     * 修改单个文件（替换文件类型必须一致）
     *
     * @param storageName 存储空间名称
     * @param inputStream 上传文件流
     * @param fileName    上传文件名称
     * @param sourcePath  替换文件存储路径
     * @param metaData    上传文件元数据
     * @return 上传文件存储路径和访问链接
     */
    BasePath modify(String storageName, InputStream inputStream, String fileName, String sourcePath,
                    Map<String, String> metaData);

    /**
     * 修改单个文件（替换文件类型必须一致）
     *
     * @param type        文件类型
     * @param inputStream 上传文件流
     * @param fileName    上传文件名称
     * @param sourcePath  替换文件存储路径
     * @param metaData    上传文件元数据
     * @return 上传文件存储路径和访问链接
     */
    BasePath modify(Integer type, InputStream inputStream, String fileName, String sourcePath,
                    Map<String, String> metaData);

    /**
     * 删除单个文件
     *
     * @param storageName 存储空间名称
     * @param sourcePath  文件存储路径
     * @return 删除结果，true 成功，false 失败
     */
    boolean delete(String storageName, String sourcePath);

    /**
     * 删除单个文件
     *
     * @param type       文件类型
     * @param sourcePath 文件存储路径
     * @return 删除结果，true 成功，false 失败
     */
    boolean delete(Integer type, String sourcePath);

    /**
     * 下载单个文件
     *
     * @param storageName 存储空间名称
     * @param sourcePath  文件存储路径
     * @return 文件流
     */
    byte[] downloadWithBytes(String storageName, String sourcePath);

    /**
     * 下载单个文件
     *
     * @param type       文件类型
     * @param sourcePath 文件存储路径
     * @return 文件流
     */
    byte[] downloadWithBytes(Integer type, String sourcePath);

    /**
     * 下载单个文件
     *
     * @param storageName 存储空间名称
     * @param sourcePath  文件存储路径
     * @return 文件流
     */
    InputStream download(String storageName, String sourcePath);

    /**
     * 下载单个文件
     *
     * @param type       文件类型
     * @param sourcePath 文件存储路径
     * @return 文件流
     */
    InputStream download(Integer type, String sourcePath);

    /**
     * 批量上传文件
     *
     * @param storageName 存储空间名称
     * @param fileList    上传文件对象列表
     * @return 上传文件存储路径和访问链接列表
     */
    List<BasePath> uploadBatch(String storageName, List<UploadFileDTO> fileList);

    /**
     * 批量上传文件
     *
     * @param type     文件类型
     * @param fileList 上传文件对象列表
     * @return 上传文件存储路径和访问链接列表
     */
    List<BasePath> uploadBatch(Integer type, List<UploadFileDTO> fileList);

    /**
     * 批量删除文件
     *
     * @param storageName    存储空间名称
     * @param sourcePathList 文件存储路径列表
     * @return 删除结果，返回删除成功的文件存储路径
     */
    List<String> deleteBatch(String storageName, List<String> sourcePathList);

    /**
     * 批量删除文件
     *
     * @param type           文件类型
     * @param sourcePathList 文件存储路径列表
     * @return 删除结果，返回删除成功的文件存储路径
     */
    List<String> deleteBatch(Integer type, List<String> sourcePathList);

    /**
     * 批量下载文件
     *
     * @param storageName    存储空间名称
     * @param sourcePathList 文件存储路径列表
     * @return 文件流
     */
    byte[] downloadBatch(String storageName, List<String> sourcePathList);

    /**
     * 批量下载文件
     *
     * @param type           文件类型
     * @param sourcePathList 文件存储路径列表
     * @return 文件流
     */
    byte[] downloadBatch(Integer type, List<String> sourcePathList);

    /**
     * 上传单张图片，并生成缩略图
     *
     * @param storageName 存储空间名称
     * @param inputStream 上传文件流
     * @param path        上传文件存储路径
     * @param fileName    上传文件名称
     * @param metaData    上传文件元数据
     * @param expiration  链接有效期
     * @return 上传文件存储路径和访问链接
     */
    BasePath uploadImage(String storageName, InputStream inputStream, String path, String fileName,
                         Map<String, String> metaData, ThumbConfig thumbConfig, Long expiration);

    /**
     * 上传单张图片，并生成缩略图
     *
     * @param type        文件类型
     * @param inputStream 上传文件流
     * @param fileName    上传文件名称
     * @param metaData    上传文件元数据
     * @return 上传文件存储路径和访问链接
     */
    BasePath uploadImage(Integer type, InputStream inputStream, String fileName, Map<String, String> metaData,
                         ThumbConfig thumbConfig, Long expiration);

    /**
     * 获取文件元信息
     *
     * @param storageName 存储空间名称
     * @param sourcePath  文件存储路径
     * @return 文件元信息
     */
    Map<String, String> getMetadata(String storageName, String sourcePath);

    /**
     * 获取文件元信息
     *
     * @param type       文件类型
     * @param sourcePath 文件存储路径
     * @return 文件元信息
     */
    Map<String, String> getMetadata(Integer type, String sourcePath);

    /**
     * 上传文件到临时目录
     *
     * @param fileDTO 上传文件
     * @return 上传文件存储路径和访问链接
     * @author linkaiwei
     * @date 2020-09-07 15:34:52
     * @since 1.5.6
     */
    BasePath uploadTemp(UploadFileDTO fileDTO);

    /**
     * 复制文件（从临时目录到正式目录）
     *
     * @param copyFilePath 要复制的文件存储路径
     * @param type         文件类型，详情见{@link FileConst.Type}
     * @return 新复制文件存储路径
     * @author linkaiwei
     * @date 2020-09-07 15:34:52
     * @since 1.5.6
     */
    BasePath copyFile(String copyFilePath, Integer type);

    /**
     * 获取请求
     * @param i
     * @param liess
     * @return
     */
    List<String> getUrlBatch(Integer type, List<String> sourcePathList);
}
