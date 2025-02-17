package com.sinohealth.system.biz.process.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 全流程异常表
 *
 * @author zegnjun 2024-08-16 18:17
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("tg_flow_process_error_log")
public class TgFlowProcessErrorLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private Long bizId;

    private String category;

    private String errorMsg;

    private Date createTime;
}
