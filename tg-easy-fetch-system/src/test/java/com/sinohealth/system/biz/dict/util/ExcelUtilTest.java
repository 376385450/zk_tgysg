package com.sinohealth.system.biz.dict.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.sinohealth.system.biz.dataassets.listener.AssetsCompareResultListener;
import com.sinohealth.system.util.EasyExcelUtil;
import org.junit.Test;

import java.io.FileOutputStream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-23 20:03
 */
public class ExcelUtilTest {


    @Test
    public void testMerge() throws Exception {


        FileOutputStream fileOutputStream = new FileOutputStream("/home/zk/App/merge.xlsx");
        ExcelWriterBuilder excelWriterBuilder = EasyExcel.write(fileOutputStream);
        ExcelWriter excelWriter = EasyExcelUtil.appendConfig(excelWriterBuilder);

        // 读取全部sheet
        // 这里需要注意 DemoDataListener的doAfterAllAnalysed 会在每个sheet读取完毕后调用一次。然后所有sheet都会往同一个DemoDataListener里面写
        EasyExcel.read("/home/zk/App/a.xlsx", new AssetsCompareResultListener(excelWriter)).doReadAll();
        EasyExcel.read("/home/zk/App/b.xlsx", new AssetsCompareResultListener(excelWriter)).doReadAll();

        excelWriter.finish();

    }
}
