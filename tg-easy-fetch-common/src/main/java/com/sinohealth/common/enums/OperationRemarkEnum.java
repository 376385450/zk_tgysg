package com.sinohealth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Huangzk
 * @date 2021/5/20 15:26
 */
@Getter
@AllArgsConstructor
public enum OperationRemarkEnum {

    DD_INSERT("字典新增"),
    DD_UPDATE("字典更新"),
    DD_DELETE("字典删除"),
    DD_CATALOG_UPDATE("字典目录变更"),
    BC_INSERT("概念新增"),
    BC_UPDATE("概念更新"),
    BC_DELETE("概念删除"),
    CR_INSERT("规则新增"),
    CR_UPDATE("规则更新"),
    CR_DELETE("规则删除");


    String name;


}
