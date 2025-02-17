package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.utils.StringUtils;
import lombok.Data;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-17 11:04
 */
@Data
public class AssetsDirRequest {

    /**
     * 项目，客户，资产
     */
    private String search;

    private List<Long> templateId;
    /**
     * 长尾打包方式
     * 注意用字符串的原因是因为这个配置不是全局唯一，而是跟着模板走，会有重名但不同含义的情况
     */
    private List<String> packTailName;
    /**
     * 时间颗粒度
     */
    private List<String> timeGra;

    private String icon;

    // normal expire deprecated
    private String expireType;

    // 内部使用
    /**
     * @see CommonConstants#MY_DATA_DIR
     */
    private Integer target;

    private List<String> icons;

    private Long applicantId;

    public boolean noSearch() {
        return StringUtils.isBlank(search);
    }

    public boolean needClean() {
        return StringUtils.isNotBlank(search) || StringUtils.isNotBlank(icon);
    }
}
