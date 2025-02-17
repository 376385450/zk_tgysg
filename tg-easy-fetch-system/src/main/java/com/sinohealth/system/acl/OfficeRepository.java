package com.sinohealth.system.acl;

import com.sinohealth.common.enums.pdf.BizType;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-10 10:15 下午
 */
public interface OfficeRepository {

    /**
     * 添加水印
     * @param multipartFile
     * @return
     */
    String watermark2Url(MultipartFile multipartFile, String watermark);

    /**
     * 添加水印
     * @param multipartFile
     * @param watermark
     * @return
     */
    InputStream watermark2Bytes(MultipartFile multipartFile, String watermark);

    /**
     * 添加水印
     * @param sourcePdfUrl
     * @param watermark
     * @return
     */
    String watermark(String sourcePdfUrl, String watermark);

    /**
     * 转换成PDF
     * @param multipartFile
     * @return
     */
    String transformPdf(MultipartFile multipartFile);

    void transformPdfAsync(String filePath, BizType bizType, String bizId);

}
