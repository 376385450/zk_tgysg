package com.sinohealth.common.utils.poi;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 导出文件工具类
 * <p>
 * Created by kaiwei on 2017/11/20.
 */
@Slf4j
public class POIUtil {

    private POIUtil() {
    }

    /**
     * 导出Excel文件
     *
     * @param title   标题名
     * @param headers 表格内容标题
     * @param dataset 表格数据
     */
    public static byte[] exportExcel2003(String title, String[] headers, Collection<Map<String, Object>> dataset) {
        if (StringUtils.isBlank(title) || ObjectUtils.isEmpty(headers) || CollectionUtils.isEmpty(dataset)) {
            return null;
        }

        // 生成一个工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();

        // 单个表格最大行数：65535
        // 限制单个表格数量，方便并行写入数据
        final int maxSize = 65534;
        // 总页数
        final int pages = (dataset.size() - 1) / maxSize + 1;
        long index = 0;

        // 并行
        List<Integer> parallelList = new ArrayList<>(pages);
        for (int page = 0; page < pages; page++) {
            parallelList.add(page);
            // 优先创建表格
            workbook.createSheet(page + 1 + "_" + title);
        }

        // 并行写入EXCEL文档
        AtomicLong finalIndex = new AtomicLong(index);
        parallelList.parallelStream().forEach(page -> {
            // 创建一个表格
            HSSFSheet sheet = workbook.getSheetAt(page);
            // 设置默认列宽
            sheet.setDefaultColumnWidth(15);

            // 表格第一行，标题行
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell;
            for (int i = 0; i < headers.length; i++) {
                cell = row.createCell(i);
                // 富文本，转码
                HSSFRichTextString text = new HSSFRichTextString(headers[i]);
                cell.setCellValue(text);
            }

            // 截取数据集
            Iterator<Map<String, Object>> it = dataset.stream().skip(finalIndex.get()).limit(maxSize).iterator();
            finalIndex.set(finalIndex.get() + maxSize);

            int x = 0;
            Object obj;
            while (it.hasNext()) {
                x++;

                row = sheet.createRow(x);

                Map<String, Object> map = it.next();
                Iterator<String> keySet = map.keySet().iterator();
                int y = 0;
                while (keySet.hasNext()) {
                    cell = row.createCell(y);

                    obj = map.get(keySet.next());
                    if (obj instanceof String) {
                        cell.setCellValue((String) obj);

                    } else if (obj instanceof BigDecimal) {
                        cell.setCellValue(((BigDecimal) obj).doubleValue());

                    } else if (obj instanceof Date) {
                        cell.setCellValue(DateFormatUtils.format((Date) obj, "yyyy-MM-dd HH:mm:ss"));

                    } else if (obj instanceof Integer) {
                        cell.setCellValue((Integer) obj);

                    } else if (obj instanceof Double) {
                        cell.setCellValue((Double) obj);

                    } else if (obj instanceof Long) {
                        cell.setCellValue((Long) obj);
                    }

                    y++;
                }
            }
        });


        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);

            return out.toByteArray();
        } catch (IOException e) {
            log.error("", e);
        } finally {
            try {
                out.close();
                workbook.close();
            } catch (IOException e) {
                log.error("", e);
            }
        }
        return null;
    }

    /**
     * 导出Excel文件
     *
     * @param title   标题名
     * @param headers 表格内容标题
     * @param dataset 表格数据
     */
    public static byte[] exportExcel2007(String title, String[] headers, Collection<Map<String, Object>> dataset) {
        if (StringUtils.isBlank(title) || ObjectUtils.isEmpty(headers) || CollectionUtils.isEmpty(dataset)) {
            return null;
        }

        // 生成一个工作簿
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        workbook.setCompressTempFiles(true);

        // 单个表格最大行数：1048576
        // 限制单个表格数量，方便并行写入数据
        final int maxSize = 1048575;
        // 总页数
        final int pages = (dataset.size() - 1) / maxSize + 1;
        long index = 0;

        // 并行
        List<Integer> parallelList = new ArrayList<>(pages);
        for (int page = 0; page < pages; page++) {
            parallelList.add(page);
            // 优先创建表格
            workbook.createSheet(page + 1 + "_" + title);
        }

        // 并行写入EXCEL文档
        AtomicLong finalIndex = new AtomicLong(index);
        parallelList.parallelStream().forEach(page -> {
            SXSSFSheet sheet = workbook.getSheetAt(page);
            // 设置默认列宽
            sheet.setDefaultColumnWidth(15);

            // 表格第一行，标题行
            SXSSFRow row = sheet.createRow(0);
            SXSSFCell cell;
            for (int i = 0; i < headers.length; i++) {
                cell = row.createCell(i);
                // 富文本，转码
                XSSFRichTextString text = new XSSFRichTextString(headers[i]);
                cell.setCellValue(text);
            }

            // 截取数据集
            Iterator<Map<String, Object>> it = dataset.stream().skip(finalIndex.get()).limit(maxSize).iterator();
            finalIndex.set(finalIndex.get() + maxSize);

            int x = 0;
            Object obj;
            while (it.hasNext()) {
                x++;
                row = sheet.createRow(x);

                Map<String, Object> map = it.next();
                Iterator<String> keySet = map.keySet().iterator();
                int y = 0;
                while (keySet.hasNext()) {
                    cell = row.createCell(y);

                    obj = map.get(keySet.next());
                    if (obj instanceof String) {
                        cell.setCellValue((String) obj);

                    } else if (obj instanceof BigDecimal) {
                        cell.setCellValue(((BigDecimal) obj).doubleValue());

                    } else if (obj instanceof Date) {
                        cell.setCellValue(DateFormatUtils.format((Date) obj, "yyyy-MM-dd HH:mm:ss"));

                    } else if (obj instanceof Integer) {
                        cell.setCellValue((Integer) obj);

                    } else if (obj instanceof Double) {
                        cell.setCellValue((Double) obj);

                    } else if (obj instanceof Long) {
                        cell.setCellValue((Long) obj);
                    }

                    y++;
                }
            }
        });

        // 写入内存中
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.error("", e);

        } finally {
            try {
                out.close();
                workbook.dispose();
                workbook.close();
            } catch (IOException e) {
                log.error("", e);
            }
        }
        return null;
    }

    /**
     * 导出Excel文件
     *
     * @param title    标题名
     * @param headers1 表格内容标题1
     * @param headers2 表格内容标题2
     * @param dataset  表格数据
     */
    public static byte[] exportExcel2007(String title, String[] headers1, String[] headers2,
                                         Collection<Map<String, Object>> dataset) {
        if (StringUtils.isBlank(title) || ObjectUtils.isEmpty(headers1) || ObjectUtils.isEmpty(headers2)
                || CollectionUtils.isEmpty(dataset)) {
            return null;
        }
        if (headers1.length != headers2.length) {
            return null;
        }

        // 生成一个工作簿
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        workbook.setCompressTempFiles(true);

        // 单个表格最大行数：1048576
        // 限制单个表格数量，方便并行写入数据
        final int maxSize = 1048575;
        // 总页数
        final int pages = (dataset.size() - 1) / maxSize + 1;
        long index = 0;

        // 并行
        List<Integer> parallelList = new ArrayList<>(pages);
        for (int page = 0; page < pages; page++) {
            parallelList.add(page);
            // 优先创建表格
            workbook.createSheet(page + 1 + "_" + title);
        }

        // 并行写入EXCEL文档
        AtomicLong finalIndex = new AtomicLong(index);
        parallelList.parallelStream().forEach(page -> {
            SXSSFSheet sheet = workbook.getSheetAt(page);
            // 设置默认列宽
            sheet.setDefaultColumnWidth(15);

            // 表格前两行，标题行
            SXSSFRow row = sheet.createRow(0);
            SXSSFCell cell;
            for (int i = 0; i < headers1.length; i++) {
                cell = row.createCell(i);
                // 富文本，转码
                XSSFRichTextString text = new XSSFRichTextString(headers1[i]);
                cell.setCellValue(text);
            }
            SXSSFRow row1 = sheet.createRow(1);
            for (int i = 0; i < headers2.length; i++) {
                cell = row1.createCell(i);
                // 富文本，转码
                XSSFRichTextString text = new XSSFRichTextString(headers2[i]);
                cell.setCellValue(text);
            }

            // 截取数据集
            Iterator<Map<String, Object>> it = dataset.stream().skip(finalIndex.get()).limit(maxSize).iterator();
            finalIndex.set(finalIndex.get() + maxSize);

            int x = 1;
            Object obj;
            while (it.hasNext()) {
                x++;
                row = sheet.createRow(x);

                Map<String, Object> map = it.next();
                Iterator<String> keySet = map.keySet().iterator();
                int y = 0;
                while (keySet.hasNext()) {
                    cell = row.createCell(y);

                    obj = map.get(keySet.next());
                    if (obj instanceof String) {
                        cell.setCellValue((String) obj);

                    } else if (obj instanceof BigDecimal) {
                        cell.setCellValue(((BigDecimal) obj).doubleValue());

                    } else if (obj instanceof Date) {
                        cell.setCellValue(DateFormatUtils.format((Date) obj, "yyyy-MM-dd HH:mm:ss"));

                    } else if (obj instanceof Integer) {
                        cell.setCellValue((Integer) obj);

                    } else if (obj instanceof Double) {
                        cell.setCellValue((Double) obj);

                    } else if (obj instanceof Long) {
                        cell.setCellValue((Long) obj);
                    }

                    y++;
                }
            }
        });

        // 写入内存中
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.error("", e);

        } finally {
            try {
                out.close();
                workbook.dispose();
                workbook.close();
            } catch (IOException e) {
                log.error("", e);
            }
        }
        return null;
    }


    public static void main(String[] args) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;
        for (int i = 0; i < 100000; i++) {
            map = new HashMap<>();
            map.put("1", i);
            map.put("2", 2000D);
            map.put("3", new Date());
            map.put("4", "测试内容，测试内容，测试内容，测试内容，测试内容，测试内容");
            map.put("5", "测试12323123232313dffsdfsdfs hello world java c zhou 开始计算时间是否是胜多负少对接方式胜多负少发的");
            map.put("6", System.currentTimeMillis());

            list.add(map);
        }

        System.out.println("开始时间：" + new Date(System.currentTimeMillis()));
        final long currentTimeMillis = System.currentTimeMillis();
        final byte[] bytes = exportExcel2007(currentTimeMillis + "",
                new String[]{"1", "2", "3", "4", "5", "6"},
                new String[]{"one", "two", "three", "four", "five", "six"},
                list);
        try {
            final File file = new File("/Users/kerv1n/Downloads/" + currentTimeMillis + ".xlsx");

            FileUtils.writeByteArrayToFile(file, bytes);
            System.out.println("结束时间：" + new Date(System.currentTimeMillis()));

        } catch (IOException e) {
            log.error("", e);
        }
    }

}
