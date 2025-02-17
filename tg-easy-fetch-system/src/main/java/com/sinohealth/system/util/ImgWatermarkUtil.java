package com.sinohealth.system.util;

import cn.hutool.core.img.ImgUtil;
import com.sinohealth.system.domain.value.deliver.DiskFile;

import java.awt.*;
import java.io.File;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-08 14:35
 */
public class ImgWatermarkUtil {

    public static File watermark(File imageFile, String watermark) {
        DiskFile tmpFile = DiskFile.createTmpFile(imageFile.getName());
        ImgUtil.pressText(imageFile, tmpFile.getFile(),
                watermark, // 水印文字
                Color.GRAY, // 字体颜色
                new Font("黑体", Font.BOLD, 100), //字体
                0, //x坐标修正值。 默认在中间，偏移量相对于中间偏移
                0, //y坐标修正值。 默认在中间，偏移量相对于中间偏移
                0.8f//透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
        );
        return tmpFile.getFile();
    }

}
