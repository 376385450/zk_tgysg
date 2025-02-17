package com.sinohealth.office;

import cn.hutool.core.io.IoUtil;
import com.sinohealth.BaseBootTest;
import com.sinohealth.system.acl.OfficeRepository;
import com.sinohealth.system.domain.value.deliver.DiskFile;
import com.sinohealth.system.domain.value.deliver.MultipartFileBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-08 15:55
 */
@ActiveProfiles("dev")
public class OfficeTest extends BaseBootTest {

    @Autowired
    private OfficeRepository officeRepository;

    @Test
    public void testWatermarkPdf() throws FileNotFoundException {
        File file = new File("/Users/fsdcyr/Downloads/测试测试.pdf");
        MultipartFile multipartFile = MultipartFileBuilder.build(new FileInputStream(file), "测试测试.pdf");
        InputStream inputStream = officeRepository.watermark2Bytes(multipartFile, "水印");
        DiskFile diskFile = DiskFile.createTmpFile("测试测试.pdf");
        IoUtil.copy(inputStream, new FileOutputStream(diskFile.getFile()));
    }
}
