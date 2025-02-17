package com.sinohealth.system.util;

import com.sinohealth.common.config.FtpProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;


@Slf4j
@Component
public class FtpClientFactory implements ApplicationContextAware, SmartInitializingSingleton {

    private static ApplicationContext applicationContext;

    private static FtpProperties ftpProperties;

    public static FtpClient getInstance() {
        return new FtpClient(ftpProperties.getServer(), ftpProperties.getPort(), ftpProperties.getUser(), ftpProperties.getPassword());
    }

    @Override
    public void afterSingletonsInstantiated() {
        FtpClientFactory.ftpProperties = applicationContext.getBean(FtpProperties.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        FtpClientFactory.applicationContext = applicationContext;
    }
}
