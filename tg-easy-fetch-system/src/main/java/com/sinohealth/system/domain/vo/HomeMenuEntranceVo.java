package com.sinohealth.system.domain.vo;/**
 * @author linshiye
 */

import lombok.Data;

/**
 * @author lsy
 * @version 1.0
 * @date 2023-04-06 10:27 下午
 */
@Data
public class HomeMenuEntranceVo {
    private String menuName;

    /** 面包屑一级id */
    private String itemLevelOneId;

    /** 面包屑一级id */
    private String itemLevelTwoId;

    private String name;

    /**
     * doc | table
     */
    private String applicationType;

    public HomeMenuEntranceVo() {
    }

    public HomeMenuEntranceVo(String menuName, String name) {
        this.menuName = menuName;
        this.name = name;
    }

    public HomeMenuEntranceVo(String menuName, String itemLevelOneId, String itemLevelTwoId, String name,
                              String applicationType) {
        this.menuName = menuName;
        this.itemLevelOneId = itemLevelOneId;
        this.itemLevelTwoId = itemLevelTwoId;
        this.name = name;
        this.applicationType = applicationType;
    }
}
