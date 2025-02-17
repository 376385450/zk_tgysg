package com.sinohealth.system.dto.application;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author Rudolph
 * @Date 2022-06-16 11:55
 * @Desc
 */
@Data
@Accessors(chain = true)
@TableName("tg_node_mapping")
public class TgNodeMapping extends Model<TgNodeMapping> {

    private Long id;

    private Long dirItemId;

    private Long nodeId;

    private Long applicantId;

    private String icon;

    public static TgNodeMapping newInstance() {
        return new TgNodeMapping();
    }

}
