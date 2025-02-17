package com.sinohealth.common.module.file.dto;

/**
 * 上传图片缩略图配置
 *
 * @author linkaiwei
 * @version v1.0
 * @date 2020/4/23 15:03
 */
public class ThumbConfig {

    /**
     * Aliyun OSS 特有配置
     * 指定缩略的模式：
     * lfit：等比缩放，限制在指定w与h的矩形内的最大图片。
     * mfit：等比缩放，延伸出指定w与h的矩形框外的最小图片。
     * fill：固定宽高，将延伸出指定w与h的矩形框外的最小图片进行居中裁剪。
     * pad：固定宽高，缩略填充。
     * fixed：固定宽高，强制缩略。
     */
    private String model;

    /**
     * 指定目标缩略图的宽度。取值范围：1 ~ 4096
     */
    private Integer width;

    /**
     * 指定目标缩略图的宽度。取值范围：1 ~ 4096
     */
    private Integer height;

    /**
     * 倍数百分比。取值范围：0 ~ 10，大于1时是放大，小于1时缩小
     */
    private Double percent;


    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }
}
