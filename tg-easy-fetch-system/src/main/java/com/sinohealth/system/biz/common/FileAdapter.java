package com.sinohealth.system.biz.common;

import cn.hutool.core.io.FileUtil;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.dataassets.AssetsExpireEnum;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.StrUtil;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsSnapshotDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.dto.FileAssetsUploadDTO;
import com.sinohealth.system.biz.dataassets.service.impl.AssetsCompareServiceImpl;
import com.sinohealth.system.config.ApplicationConfigTypeConstant;
import com.sinohealth.system.config.FileProperties;
import com.sinohealth.system.util.FtpClient;
import com.sinohealth.system.util.FtpClientFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Kuangcp
 * 2024-09-10 16:04
 */
@Slf4j
@Service
@AllArgsConstructor
public class FileAdapter {

    private final FileProperties fileProperties;
    private final Environment environment;

    private final UserDataAssetsDAO assetsDAO;
    private final UserDataAssetsSnapshotDAO snapshotAssetsDAO;

    /**
     * 表资产 导出Excel/csv
     */
    public String buildAssetsPath(Long assetsId, String suffix) {
        String time = DateUtils.dateTimeNow();
        return String.format("%s/%s/assets/%d/%s.%s", fileProperties.getFtpPrefix(),
                environment.getActiveProfiles()[0], assetsId, StrUtil.randomAlpha(6) + "_" + time, suffix);
    }

    /**
     * 清理测试环境所有资产文件
     */
    public void cleanAllAssets() {
        String env = environment.getActiveProfiles()[0];
        if (!Objects.equals(env, "test")) {
            return;
        }

        String root = String.format("%s/test/assets", fileProperties.getFtpPrefix());
        try (FtpClient ftpClient = FtpClientFactory.getInstance()) {
            ftpClient.open();
            Collection<String> assetsList = ftpClient.listFiles(root);
            for (String ad : assetsList) {
                log.info("del aid: ad={}", ad);
                String dir = root + "/" + ad;
                Collection<String> fs = ftpClient.listFiles(dir);
                for (String f : fs) {
                    ftpClient.delete(dir + "/" + f);
                }
                ftpClient.deleteDir(dir);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }


    /**
     * TODO 删除未绑定为资产路径的临时文件
     */
    public String cleanAttachFile() {
        // 查出资产表全部有效的路径
        // 查出FTP attach路径全部文件
        // 差集 删除未使用的文件
        assetsDAO.lambdaQuery()
                .select(UserDataAssets::getFtpPath)
                .eq(UserDataAssets::getConfigType, ApplicationConfigTypeConstant.FILE_TYPE)
                .ne(UserDataAssets::getExpireType, AssetsExpireEnum.delete_data.name())
                .list();

        snapshotAssetsDAO.lambdaQuery()
                .list();
        return "OK";
    }

    /**
     * 文件资产 附件
     */
    public AjaxResult<FileAssetsUploadDTO> uploadToFTP(MultipartFile file) {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        if (com.sinohealth.common.utils.StringUtils.isBlank(originalFilename)) {
            return AjaxResult.error("文件名为空");
        }
        String suffix = FileUtil.getSuffix(originalFilename);
        String path = this.buildAttachPath(suffix);
        try (FtpClient ftpClient = FtpClientFactory.getInstance()) {
            ftpClient.open();
            ftpClient.uploadFile(path, file.getInputStream());
            return AjaxResult.success(new FileAssetsUploadDTO().setPath(path).setName(originalFilename));
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    private String buildAttachPath(String suffix) {
        String date = DateUtils.getDateYM();
        return String.format("%s/%s/attach/%s/%s.%s", fileProperties.getFtpPrefix(),
                environment.getActiveProfiles()[0], date, UUID.randomUUID(), suffix);
    }

    /**
     * 特殊对比 临时文件处理
     */
    @Scheduled(cron = "0 0 22 * * ?")
    public void cleanTempFile() {
        String tempDir = buildTempCompareDir();
        try (FtpClient ftp = FtpClientFactory.getInstance()) {
            ftp.open();

            Collection<String> file = ftp.listFiles(tempDir);
            for (String s : file) {
                try {
                    ftp.delete(tempDir + "/" + s);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private String buildTempCompareDir() {
        return String.format("%s/%s/compf/tmp", fileProperties.getFtpPrefix(), environment.getActiveProfiles()[0]);
    }

    /**
     * @see AssetsCompareServiceImpl#rename 创建对比任务时 搬文件目录
     */
    public String uploadTempCompareFile(MultipartFile file, String suffix) {
        String remote = this.buildTempComparePath(suffix);
        FtpClient ftpClient = FtpClientFactory.getInstance();
        try {
            ftpClient.open();
            ftpClient.uploadFile(remote, file.getInputStream());
        } catch (IOException e) {
            log.error("", e);
        } finally {
            try {
                ftpClient.close();
            } catch (Exception e) {
                log.error("ftp关闭失败", e);
            }
        }
        return remote;
    }

    private String buildTempComparePath(String suffix) {
        return String.format("%s/%s/compf/tmp/%s.%s", fileProperties.getFtpPrefix(),
                environment.getActiveProfiles()[0], UUID.randomUUID(), suffix);
    }
}
