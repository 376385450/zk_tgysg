package com.sinohealth.system.domain.value.deliver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import com.sinohealth.saas.office.exception.OfficeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 磁盘文件
 *
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-09 1:42 下午
 */
@Slf4j
public class DiskFile implements Serializable {

    public static final String TMP_DIR = FileUtil.getTmpDirPath();

    private static final String DIR;

    /**
     * /tmp/office-file/
     */
    static {
        DIR = TMP_DIR + File.separator + "tg-ysg-file" + File.separator;
        log.info("DiskFile-dir: {}", DIR);
        if (!FileUtil.exist(DIR)) {
            FileUtil.mkdir(DIR);
        }
    }

    private File file;

    private DiskFile(File file) {
        log.info("DiskFile.path: {}", file.getPath());
        this.file = file;
    }

    public static DiskFile createFromURL(String url) {
        // 从URL中获取文件名称
        String fileName = FileUtil.getName(URLUtil.getPath(url));
        String localFileAbsolutePath = DIR + generateFileDir() + File.separator + fileName;
        HttpUtil.downloadFile(url, localFileAbsolutePath);
        File file = new File(localFileAbsolutePath);
        DiskFile diskFile = new DiskFile(file);
        return diskFile;
    }

    public static DiskFile createFromMultipartFile(MultipartFile multipartFile) {
        try {
            String path = DIR + generateFileDir();
            FileUtil.mkdir(path);
            File file = new File(path, multipartFile.getOriginalFilename());
            log.info("DiskFile.createFromMultipartFile: {}", file.getPath());
            IoUtil.copy(multipartFile.getInputStream(), new FileOutputStream(file));
            DiskFile diskFile = new DiskFile(file);
            return diskFile;
        } catch (Exception e) {
            log.error("", e);
            throw new OfficeException(e);
        }
    }

    public static DiskFile createTmpFile(String fileName) {
        String tmpFileDirPath = DIR + generateFileDir();
        FileUtil.mkdir(tmpFileDirPath);
        String localFileAbsolutePath = tmpFileDirPath + File.separator + fileName;
        File file = new File(localFileAbsolutePath);
        DiskFile diskFile = new DiskFile(file);
        return diskFile;
    }

    public static DiskFile getExistsFile(String filePath) {
        DiskFile diskFile = new DiskFile(new File(filePath));
        return diskFile;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static String generateFileDir() {
        String time = LocalDateTime.now(ZoneId.systemDefault()).format(FORMATTER);
        return IdUtil.fastSimpleUUID() + "_" + time;
    }

    public File getFile() {
        return file;
    }

    public void destroy() {
        try {
            if (FileUtil.exist(file)) {
                FileUtil.del(file);
            }
        } catch (Exception e) {

        }
    }


}
