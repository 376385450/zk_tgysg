package com.sinohealth.system.util;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.style.row.SimpleRowHeightStyleStrategy;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-04-17 10:46
 */
public class EasyExcelUtil {

    public static ExcelWriter appendConfig(ExcelWriterBuilder excelWriterBuilder) {
        return excelWriterBuilder
                .useDefaultStyle(false)
                .registerWriteHandler(getStyleStrategy())
                .registerWriteHandler(new SimpleRowHeightStyleStrategy((short)18, (short)18))
                .build();
    }

    //设置样式 去除默认表头样式及设置内容居中
    public static HorizontalCellStyleStrategy getStyleStrategy() {
        //内容样式策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        WriteFont contentFont = new WriteFont();
        contentFont.setFontName("宋体");
        contentFont.setFontHeightInPoints((short) 11);
        contentWriteCellStyle.setWriteFont(contentFont);

        //头策略使用默认
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        headWriteCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        WriteFont headerFont = new WriteFont();
        headerFont.setFontName("宋体");
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setBold(true);
        headWriteCellStyle.setWriteFont(headerFont);

        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
    }
}
