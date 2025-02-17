package com.sinohealth.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 压缩工具类
 *
 * @author linkaiwei
 * @version v1.0
 * @date 2020/4/21 09:18
 */
public class CompressionUtil {

    private CompressionUtil() {
    }

    /**
     * 压缩，deflate 的实现方式，使用 DEFLATE 算法，文件后缀为 .deflate
     * <p>
     * JDK原生支持，不需要第三方依赖
     *
     * @param input 待压缩字节数组
     * @return 压缩完成字节数组
     */
    public static byte[] compressByDeflate(byte[] input) {
        return compressByDeflate(input, Deflater.DEFAULT_COMPRESSION);
    }

    /**
     * 压缩，deflate 的实现方式，使用 DEFLATE 算法，文件后缀为 .deflate
     * <p>
     * JDK原生支持，不需要第三方依赖
     *
     * @param input 待压缩字节数组
     * @param level 压缩级别，1 ~ 9，压缩效率从低到高，压缩时间也会随之增加
     * @return 压缩完成字节数组
     */
    public static byte[] compressByDeflate(byte[] input, int level) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Deflater compressor = new Deflater(level);
        try {
            compressor.setInput(input);
            compressor.finish();
            final byte[] buf = new byte[2048];
            while (!compressor.finished()) {
                int count = compressor.deflate(buf);
                bos.write(buf, 0, count);
            }
        } finally {
            compressor.end();
        }

        return bos.toByteArray();
    }

    /**
     * 解压，deflate 的实现方式，使用 DEFLATE 算法
     * <p>
     * JDK原生支持，不需要第三方依赖
     *
     * @param input 待解压字节数组
     * @return 解压后字节数组
     * @throws DataFormatException 数据格式异常，不是 deflate 压缩格式
     */
    public static byte[] decompressByDeflate(byte[] input) throws DataFormatException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Inflater decompressor = new Inflater();
        try {
            decompressor.setInput(input);
            final byte[] buf = new byte[2048];
            while (!decompressor.finished()) {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            }
        } finally {
            decompressor.end();
        }

        return bos.toByteArray();
    }

    /**
     * 压缩，Gzip 的实现方式，使用 DEFLATE 算法，文件后缀为 .gz
     * <p>
     * JDK原生支持，不需要第三方依赖
     *
     * @param bytes 待压缩字节数组
     * @return 压缩完成字节数组
     */
    public static byte[] compressByGzip(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(bytes);
        gzip.close();

        return out.toByteArray();
    }

    /**
     * 解压，Gzip 的实现方式，使用 DEFLATE 算法
     * <p>
     * JDK原生支持，不需要第三方依赖
     *
     * @param bytes 待解压字节数组
     * @return 解压后字节数组
     */
    public static byte[] decompressByGzip(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);

        GZIPInputStream gzipInputStream = new GZIPInputStream(in);
        byte[] buffer = new byte[2048];
        int n;
        while ((n = gzipInputStream.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
        }

        return out.toByteArray();
    }

    /**
     * 压缩，Zip 的实现方式
     *
     * @param fileInfoList 待解压字节数组列表
     * @return 解压后字节数组
     */
    public static byte[] compressByZip(List<ZipFileInfo> fileInfoList) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        for (ZipFileInfo fileInfo : fileInfoList) {
            ZipEntry zipEntry = new ZipEntry(fileInfo.name);
            if (Objects.nonNull(fileInfo.getBytes())) {
                zipEntry.setSize(fileInfo.getBytes().length);
            }
            zipOutputStream.putNextEntry(zipEntry);

            if (Objects.nonNull(fileInfo.getBytes())) {
                zipOutputStream.write(fileInfo.getBytes());
            }
        }
        zipOutputStream.closeEntry();
        zipOutputStream.close();

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Zip文件信息
     */
    public static class ZipFileInfo {

        /**
         * 文件名称
         */
        private String name;

        /**
         * 文件字节流
         */
        private byte[] bytes;


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }
    }

}
