package com.sinohealth.common.enums.dataassets;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

/**
 * @Author shallwetalk
 * @Date 2023/9/18
 */
@Getter
@AllArgsConstructor
public enum FileTypeEnum {

    PDF("PDF"),
    EXCEL("Excel"),
    PPT("PPT"),
    WORD("Word"),
    OTHER("其他");

    private String desc;


    public static String getAssetType(String assetType) {
        if (Arrays.asList("XLS", "XLSX").contains(assetType.toUpperCase(Locale.ROOT))) {
            return EXCEL.desc;
        }
        if (Arrays.asList("PDF").contains(assetType.toUpperCase(Locale.ROOT))) {
            return PDF.desc;
        }
        if (Arrays.asList("PPT", "PPTX").contains(assetType.toUpperCase(Locale.ROOT))) {
            return PPT.desc;
        }
        if (Arrays.asList("DOC", "DOCX").contains(assetType.toUpperCase(Locale.ROOT))) {
            return WORD.desc;
        }

        return OTHER.desc;
    }

}
