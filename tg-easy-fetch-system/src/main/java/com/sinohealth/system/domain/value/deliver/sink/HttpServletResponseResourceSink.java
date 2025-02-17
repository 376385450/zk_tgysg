package com.sinohealth.system.domain.value.deliver.sink;

import cn.hutool.core.io.IoUtil;
import com.sinohealth.system.domain.value.deliver.DeliverResourceType;
import com.sinohealth.system.domain.value.deliver.Resource;
import com.sinohealth.system.domain.value.deliver.ResourceSink;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-29 09:10
 */
@Slf4j
public class HttpServletResponseResourceSink implements ResourceSink<HttpServletResponseResourceSink, Void> {

    private Resource resource;

    private DeliverResourceType type;

    private HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getResponse();
    }

    @Override
    public HttpServletResponseResourceSink setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public HttpServletResponseResourceSink setType(DeliverResourceType type) {
        this.type = type;
        return this;
    }

    @Override
    public DeliverResourceType getType() {
        return type;
    }

    @Override
    public Void process() {
        if (resource == null) {
            return null;
        }
        HttpServletResponse response = getResponse();
        switch (type) {
            case EXCEL: {
                writeExcel(response);
                break;
            }
            case CSV: {
                writeCsv(response);
                break;
            }
            case PDF: {
                writePdf(response);
                break;
            }
            case IMAGE: {
                writeImage(response);
                break;
            }
            case ZIP: {
                writeZip(response);
                break;
            }
            default: {

            }
        }
        return null;
    }

    @SneakyThrows
    private void writeExcel(HttpServletResponse response) {
        InputStream inputStream = resource.getInputStream();
        String fileName = resource.getName();
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);
        IoUtil.copy(inputStream, response.getOutputStream());
    }

    @SneakyThrows
    private void writeCsv(HttpServletResponse response) {
        InputStream inputStream = resource.getInputStream();
        String fileName = resource.getName();
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/csv");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);
        IoUtil.copy(inputStream, response.getOutputStream());
    }

    @SneakyThrows
    private void writePdf(HttpServletResponse response) {
        InputStream inputStream = resource.getInputStream();
        String fileName = resource.getName();
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/octet-stream");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);
        IoUtil.copy(inputStream, response.getOutputStream());
    }

    @SneakyThrows
    private void writeImage(HttpServletResponse response) {
        InputStream inputStream = resource.getInputStream();
        String fileName = resource.getName();
        response.setCharacterEncoding("utf-8");
        response.setContentType("image/png");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);
        IoUtil.copy(inputStream, response.getOutputStream());
    }

    @SneakyThrows
    private void writeZip(HttpServletResponse response) {
        InputStream inputStream = resource.getInputStream();
        String fileName = resource.getName();
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/x-msdownload");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);
        IoUtil.copy(inputStream, response.getOutputStream());
    }
}
