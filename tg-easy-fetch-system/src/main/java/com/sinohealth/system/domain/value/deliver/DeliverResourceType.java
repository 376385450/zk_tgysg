package com.sinohealth.system.domain.value.deliver;


/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 17:41
 */
public enum DeliverResourceType {

    CUSTOMER,

    EMAIL,

    CSV,

    EXCEL,

    PDF,

    IMAGE,

    ZIP,

    FILE,

    DOC,

    DOCX,

    PPT,

    PPTX;

    public static DeliverResourceType fromName(String name) {
        String upper = name.toUpperCase();
        for (DeliverResourceType type : DeliverResourceType.values()) {
            if (type.name().equalsIgnoreCase(upper)) {
                return type;
            }
        }
        throw new IllegalArgumentException("交付类型名不匹配:" + name);
    }

    public static DeliverResourceType match(String name) {
        String upper = name.toUpperCase();
        for (DeliverResourceType type : DeliverResourceType.values()) {
            if (type.name().equalsIgnoreCase(upper)) {
                return type;
            }
        }
        if(upper.equalsIgnoreCase("xlsx") || upper.equalsIgnoreCase("xls")){
            return DeliverResourceType.EXCEL;
        }
        if(upper.equalsIgnoreCase("png") || upper.equalsIgnoreCase("jpeg")){
            return DeliverResourceType.IMAGE;
        }
        return DeliverResourceType.FILE;
    }

}
