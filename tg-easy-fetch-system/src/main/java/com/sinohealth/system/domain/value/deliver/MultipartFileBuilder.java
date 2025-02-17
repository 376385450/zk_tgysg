package com.sinohealth.system.domain.value.deliver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import com.sinohealth.saas.office.exception.OfficeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * DiskFileItem 会产生临时文件
 * @see DiskFileItem#getTempFile()
 *
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-09 11:38 上午
 */
@Slf4j
public class MultipartFileBuilder {

    /**
     * 直接将inputStream拷贝到fileItem有问题
     * @param inputStream
     * @param fileName
     * @return
     */
    public static MultipartFile build(InputStream inputStream, String fileName) {
        try {
            // 保存到磁盘
            DiskFile diskFile = DiskFile.createTmpFile(fileName);
            File file = diskFile.getFile();
            IoUtil.copy(inputStream, new FileOutputStream(file));
            FileItem fileItem = new DiskFileItem("file", Files.probeContentType(file.toPath()), false, file.getName(), (int) file.length(), file.getParentFile());
            OutputStream outputStream = fileItem.getOutputStream();
            IoUtil.copy(new FileInputStream(file), outputStream);
            return new CommonsMultipartFile(fileItem);
        } catch (Exception ex) {
            throw new OfficeException(ex);
        }
    }

    public static MultipartFile build(File file) {
        try {
            log.info("用户目录: {}, 文件路径: {}, 文件绝对路径: {}, 是否是绝对路径: {}, 是否可读: {}, 是否存在: {}", FileUtil.getUserHomePath(), file.getPath(), file.getAbsolutePath(), file.isAbsolute(), file.canRead(), file.exists());
            FileInputStream inputStream = new FileInputStream(file);
            FileItem fileItem = new DiskFileItem("file", Files.probeContentType(file.toPath()), false, file.getName(), (int) file.length(), file.getParentFile());
            OutputStream outputStream = fileItem.getOutputStream();
            IoUtil.copy(inputStream, outputStream);
            return new CommonsMultipartFile(fileItem);
        } catch (IOException ex) {
            // do something.
            throw new OfficeException(ex);
        }
    }

}
