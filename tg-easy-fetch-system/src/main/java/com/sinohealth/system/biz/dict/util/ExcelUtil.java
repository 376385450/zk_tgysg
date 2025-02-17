package com.sinohealth.system.biz.dict.util;

import com.sinohealth.system.domain.value.deliver.DiskFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-21 14:18
 */
public class ExcelUtil {

    public static List<List<String>> head(String[] headMap) {
        List<List<String>> list = new ArrayList<>();

        for (String head : headMap) {
            List<String> headList = new ArrayList<>();
            headList.add(head);
            list.add(headList);
        }
        return list;
    }

    public static List<List<String>> head(String[] headMap, String... append) {
        List<List<String>> list = new ArrayList<>();
        if (Objects.nonNull(append)) {
            for (String head : append) {
                List<String> headList = new ArrayList<>();
                headList.add(head);
                list.add(headList);
            }
        }

        for (String head : headMap) {
            List<String> headList = new ArrayList<>();
            headList.add(head);
            list.add(headList);
        }

        return list;
    }

    public static void mergeFile(List<DiskFile> allFiles) {
//        ExcelWriterSheetBuilder builder = EasyExcel.write("").head(null).sheet("模板");
//        builder.doWrite();
//        builder.doWrite();
//        builder.build();
    }

}
