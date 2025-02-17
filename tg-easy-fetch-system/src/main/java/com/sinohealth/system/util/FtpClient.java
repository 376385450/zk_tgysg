package com.sinohealth.system.util;

import cn.hutool.core.io.FileUtil;
import com.sinohealth.common.exception.FTPException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * https://www.baeldung.com/java-ftp-client
 * <p>
 * 2023-12-19 14:18
 */
@Slf4j
public class FtpClient implements Closeable {

    private final String server;
    private final int port;
    private final String user;
    private final String password;
    private FTPClient ftp;

    public FtpClient(String server, int port, String user, String password) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
    }


    public void enterLocalPassiveMode() {
        ftp.enterLocalPassiveMode();
    }

    public void open() throws IOException {
        ftp = new FTPClient();

        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        ftp.connect(server, port);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new IOException("Exception in connecting to FTP Server");
        }

        boolean login = ftp.login(user, password);
        log.info("login={}", login);
        // 被动模式
        ftp.enterLocalPassiveMode();
        ftp.setControlEncoding("utf-8");
    }

    public void close() throws IOException {
        ftp.disconnect();
    }

    public Collection<String> listFiles(String path) throws IOException {
        // 如果本地启动了ftp服务，需要设置，否则会报425 https://stackoverflow.com/questions/72452185/java-ftp-client-failing-with-425-failed-to-establish-connection
        ftp.enterLocalPassiveMode();

        FTPFile[] files = ftp.listFiles(path);
        return Arrays.stream(files)
                .map(FTPFile::getName)
                .collect(Collectors.toList());
    }

    public Collection<String> listFiles(String path, final FTPFileFilter filter) throws IOException {
        FTPFile[] files = ftp.listFiles(path, filter);
        return Arrays.stream(files)
                .map(FTPFile::getName)
                .collect(Collectors.toList());
    }

    public void delete(String path) throws IOException {
        ftp.deleteFile(path);
    }
    public void deleteDir(String path) throws IOException {
        ftp.removeDirectory(path);
    }

    public void download(String source, OutputStream outputStream) throws IOException {
        ftp.retrieveFile(source, outputStream);
    }

    void putFileToPath(File file, String path) throws IOException {
        ftp.storeFile(path, Files.newInputStream(file.toPath()));
    }

    /**
     * 上传文件到ftp
     *
     * @param remote
     * @param in     输入流
     */
    @SneakyThrows
    public void uploadFile(String remote, InputStream in) {
        String dir = FileUtil.getParent(remote, 1);
        this.ensureDir(dir);
        // 进入文件目录
        ftp.changeWorkingDirectory(dir);
        // 设置缓冲区大小
        ftp.setBufferSize(1024);
        // 设置文件类型
        ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
        // 文件上传
        String uploadFileName = FileUtil.getName(remote);
        ftp.storeFile(uploadFileName, in);
        log.info("上传文件响应码: {}, 上传文件响应信息: {}", ftp.getReplyCode(), ftp.getReplyString());
    }

    private void ensureDir(String dir) throws IOException {
        boolean enter = ftp.changeWorkingDirectory(dir);
        if (enter) {
            return;
        }
        log.info("没有目录: {}", dir);
        if (!ftp.makeDirectory(dir)) {
            log.warn("创建文件目录【{}】失败", dir);
            throw new FTPException("创建文件目录" + dir + "失败");
        }
    }

    @SneakyThrows
    public void downloadFile(String remote, OutputStream out) {
        String osName = System.getProperty("os.name").toLowerCase();
        String dir;
        if (osName.contains("win")) {
            Path path = Paths.get(remote);
            // 获取文件夹路径
            Path folderPath = path.getParent();
            dir = folderPath.toString();
        } else {
            dir = FileUtil.getParent(remote, 1);
        }
        String filename = FileUtil.getName(remote);
        if (!ftp.changeWorkingDirectory(dir)) {
            throw new FTPException("ftp目录不存在" + dir);
        }
        FTPFile[] ftpFiles = ftp.listFiles();
        if (ftpFiles.length < 1) {
            throw new FTPException("目录为空");
        }
        for (FTPFile ftpFile : ftpFiles) {
            if (filename.equalsIgnoreCase(ftpFile.getName())) {
                ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftp.retrieveFile(filename, out);
                break;
            }
        }
    }

    @SneakyThrows
    public InputStream downloadFile(String remote) {
        String osName = System.getProperty("os.name").toLowerCase();
        String dir;
        if (osName.contains("win")) {
            Path path = Paths.get(remote);
            // 获取文件夹路径
            Path folderPath = path.getParent();
            dir = folderPath.toString();
        } else {
            dir = FileUtil.getParent(remote, 1);
        }
        String filename = FileUtil.getName(remote);
        if (!ftp.changeWorkingDirectory(dir)) {
            throw new FTPException("ftp目录不存在" + dir);
        }
        FTPFile[] ftpFiles = ftp.listFiles();
        if (ftpFiles.length < 1) {
            throw new FTPException("目录为空");
        }
        for (FTPFile ftpFile : ftpFiles) {
            if (filename.equalsIgnoreCase(ftpFile.getName())) {
                ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
                return ftp.retrieveFileStream(filename);
            }
        }
        return null;
    }


    /**
     * @param from 注意都是绝对路径
     */
    @SneakyThrows
    public void rename(String from, String to) {
        String dir = FileUtil.getParent(to, 1);
        this.ensureDir(dir);
        boolean result = ftp.rename(from, to);
        if (!result) {
            log.info("rename failed {} {}", from, to);
        }
    }

}
