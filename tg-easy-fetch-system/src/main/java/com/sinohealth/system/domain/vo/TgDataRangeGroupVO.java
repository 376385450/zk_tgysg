package com.sinohealth.system.domain.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author zhangyanping
 * @date 2023/5/15 16:16
 */
@Data
@ToString
public class TgDataRangeGroupVO {
    private String groupId;
    private List<TgDataRangeTemplateVO> data;
}
