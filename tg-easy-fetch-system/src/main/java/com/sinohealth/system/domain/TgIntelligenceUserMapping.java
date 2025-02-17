package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author shallwetalk
 * @Date 2023/8/5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_intelligence_user_mapping")
public class TgIntelligenceUserMapping {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "tg_user_id")
    private Long tgUserId;

    @TableField(value = "ysg_user_id")
    private Long ysgUserId;

}
