package com.sinohealth.system.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-01-22 11:36
 */
@Slf4j
public class FtpClientTest {

    private FtpClient ftpClient;

    @Before
    public void setup() throws IOException {
//        ftpClient = new FtpClient("localhost", 2121, "test", "test");
        ftpClient = new FtpClient("192.168.16.157", 21, "test_fetch", "sinohealth");
        ftpClient.open();
    }

    @After
    public void teardown() throws IOException {
        ftpClient.close();
    }

    @Test
    public void testList() throws IOException {
        Collection<String> files = ftpClient.listFiles("/ftp");
//        assertThat(files).contains("foobar.txt");
        for (String file : files) {
            log.info(file);
        }
    }

    @Test
    public void testSearch() throws Exception {
        String key = "java";
        FTPFileFilter filter = a -> {
            if (Objects.equals(FTPFile.DIRECTORY_TYPE, a.getType())) {
                System.out.println(a.getName());
            }
            return StringUtils.containsIgnoreCase(a.getName(), key);
        };
        ftpClient.enterLocalPassiveMode();

        Collection<String> files = ftpClient.listFiles("/ftp", filter);
        for (String file : files) {
            log.info(file);
        }
    }

    @Test
    public void testDelete() throws Exception {
        ftpClient.delete("/ftp/data-quality/settings.gradle");
    }


    private AtomicInteger c = new AtomicInteger();

    @Test
    public void testSearchRecursive() throws Exception {
        ftpClient.enterLocalPassiveMode();
        List<String> files = this.search("/ftp", "java");
        for (String file : files) {
//            log.info(file);
        }
        System.out.println(c.get());
    }

    public List<String> search(String path, String key) throws IOException {
        List<String> result = new ArrayList<>();


        log.info("path={}", path);
        c.incrementAndGet();
        FTPFileFilter filter = a -> {
            if (Objects.equals(FTPFile.DIRECTORY_TYPE, a.getType())) {
                System.out.println(a.getName());
                try {
                    String dir = path + "/" + a.getName();
                    List<String> next = this.search(dir, key);
                    if (!next.isEmpty()) {
                        for (String f : next) {
                            if (f.contains("/")) {
                                result.add(f);
                            } else {
                                result.add(path + "/" + f);
                            }
                        }
                    }
                } catch (IOException e) {
                    log.error("", e);
                }
            }
            return StringUtils.containsIgnoreCase(a.getName(), key);
        };
        Collection<String> files = ftpClient.listFiles(path, filter);
        result.addAll(files);

        return result;
    }

    @Test
    public void testUpload() throws IOException {
        ftpClient.open();
        FileInputStream fis = new FileInputStream(new File("/Users/fsdcyr/Downloads/F25.xlsx"));
        ftpClient.uploadFile("/ftp/test/F25.xlsx", fis);
        ftpClient.close();
    }

    @Test
    public void testDownload() throws IOException {
        ftpClient.open();
        FileOutputStream fos = new FileOutputStream(new File("/Users/fsdcyr/Downloads/F26.xlsx"));
        ftpClient.downloadFile("/ftp/tg-easy-fetch/assets/1060/581ecad4-14d3-41fe-81f7-19269940be4c/F26.xlsx", fos);
        ftpClient.close();
    }
}