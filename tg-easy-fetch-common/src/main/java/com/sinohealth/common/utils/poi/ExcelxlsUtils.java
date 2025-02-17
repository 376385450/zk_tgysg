package com.sinohealth.common.utils.poi;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sinohealth.common.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * @author Jingjun
 * @since 2021/5/14
 */
@Slf4j
public class ExcelxlsUtils {

    private static long MAX_SHEET_NUM = 20;
    private static long PAGE_SHEET_NUM = 60000;

    public static void writeExcel2003(File file, String sheetName, String[] header, List<Map<String, Object>> data)
            throws IOException {

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(sheetName);

        // 写入内容
        Iterator<Map<String, Object>> iterator = data.iterator();
        HSSFRow headRow = sheet.createRow(0);
        for (int i = 0; i < header.length; i++) {
            HSSFCell cell = headRow.createCell(i);
            cell.setCellValue(header[i]);
        }
        int index = 1;
        while (iterator.hasNext()) {
            HSSFRow row = sheet.createRow(index);
            Map<String, Object> map = iterator.next();

            for (int i = 0; i < header.length; i++) {

                HSSFCell cell = row.createCell(i);
                try {
                    Object val = map.get(header[i]);
                    if (val == null) {
                        cell.setCellValue("");
                    } else if (val instanceof Date) {
                        cell.setCellValue(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, (Date) val));
                    } else {
                        cell.setCellValue(val.toString());
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
            index++;
        }

        OutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);

        outputStream.close();

    }

    public static void writeExcel2003More(File file, String sheetName, String[] header, List<Map<String, Object>> data)
            throws IOException {
        int pageNum = 1;
        int pageSize = (int) PAGE_SHEET_NUM;
        HSSFWorkbook workbook = new HSSFWorkbook();
        while (true){
            PageInfo<Map<String, Object>> item = ExcelxlsUtils.list2PageInfo(data,pageNum,pageSize);
            if(CollectionUtils.isEmpty(item.getList())){
                break;
            }
            // sheetName 不能超过32个字符，超过会截取前32个字符判断是否重复
//            HSSFSheet sheet = workbook.createSheet(sheetName + "_" + pageNum);
            HSSFSheet sheet = workbook.createSheet(pageNum + "_" + sheetName);
            HSSFRow headRow = sheet.createRow(0);
            for (int i = 0; i < header.length; i++) {
                HSSFCell cell = headRow.createCell(i);
                cell.setCellValue(header[i]);
            }
            // 写入内容
            Iterator<Map<String, Object>> iterator = item.getList().iterator();
            int index = 1;
            while (iterator.hasNext()) {
                HSSFRow row = sheet.createRow(index);
                Map<String, Object> map = iterator.next();
                for (int i = 0; i < header.length; i++) {

                    HSSFCell cell = row.createCell(i);
                    try {
                        Object val = map.get(header[i]);
                        if (val == null) {
                            cell.setCellValue("");
                        } else if (val instanceof Date) {
                            cell.setCellValue(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, (Date) val));
                        } else {
                            cell.setCellValue(val.toString());
                        }
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
                index++;
            }
            pageNum++;
        }
        OutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);
        outputStream.close();
    }

    public static <T> PageInfo<T> list2PageInfo(List<T> arrayList, Integer pageNum, Integer pageSize) {
        //实现list分页
        PageHelper.startPage(pageNum, pageSize);
        int pageStart = pageNum == 1 ? 0 : (pageNum - 1) * pageSize;
        int pageEnd = arrayList.size() < pageSize * pageNum ? arrayList.size() : pageSize * pageNum;
        List<T> pageResult = new LinkedList<T>();
        if (arrayList.size() > pageStart) {
            pageResult = arrayList.subList(pageStart, pageEnd);
        }
        PageInfo<T> pageInfo = new PageInfo<T>(pageResult);
        //获取PageInfo其他参数
        pageInfo.setTotal(arrayList.size());
        int endRow = (int)pageInfo.getEndRow() == 0 ? 0 : (int) ((pageNum - 1) * pageSize + pageInfo.getEndRow() + 1);
        pageInfo.setEndRow(endRow);
        boolean hasNextPage = arrayList.size() <= pageSize * pageNum ? false : true;
        pageInfo.setHasNextPage(hasNextPage);
        boolean hasPreviousPage = pageNum == 1 ? false : true;
        pageInfo.setHasPreviousPage(hasPreviousPage);
        pageInfo.setIsFirstPage(!hasPreviousPage);
        boolean isLastPage = (arrayList.size() > pageSize * (pageNum - 1) && arrayList.size() <= pageSize * pageNum) ? true : false;
        pageInfo.setIsLastPage(isLastPage);
        int pages = arrayList.size() % pageSize == 0 ? arrayList.size() / pageSize : (arrayList.size() / pageSize) + 1;
        pageInfo.setNavigateLastPage(pages);
        int[] navigatePageNums = new int[pages];
        for (int i = 1; i < pages; i++) {
            navigatePageNums[i - 1] = i;
        }
        pageInfo.setNavigatepageNums(navigatePageNums);
        int nextPage = pageNum < pages ? pageNum + 1 : 0;
        pageInfo.setNextPage(nextPage);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setPages(pages);
        pageInfo.setPrePage(pageNum - 1);
        pageInfo.setSize(pageInfo.getList().size());
        int starRow = arrayList.size() < pageSize * pageNum ? 1 + pageSize * (pageNum - 1) : 0;
        pageInfo.setStartRow(starRow);
        return pageInfo;
    }

}
