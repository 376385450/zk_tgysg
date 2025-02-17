package com.sinohealth.common.utils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2024-01-23 18:31
 */
public class HttpServletResponseUtil {

    public static void setting(String file, HttpServletResponse response) throws IOException {
        String fileNameEncode = URLEncoder.encode(file, "UTF-8").replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileNameEncode);
        response.setHeader("Pragma", "public");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Expires", "0");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
    }

    public static void settingZip(String file, Integer contentLength, HttpServletResponse response) throws IOException {
        String fileNameEncode = URLEncoder.encode(file, "UTF-8").replaceAll("\\+", "%20");
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileNameEncode);
        response.setHeader("Pragma", "public");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Expires", "0");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
//        response.setContentLength(contentLength);
    }

}
