package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-04-23 13:39
 */
@Data
@TableName(value = "tg_fast_entry", autoResultMap = true)
public class FastEntry {

    /**
     * 自增id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String menuName;
    private String icon;
    private Integer sort;

    public FastEntry() {

    }

    public FastEntry(String name, String menuName, String icon, Integer sort) {
        this.name = name;
        this.menuName = menuName;
        this.icon = icon;
        this.sort = sort;
    }
}
