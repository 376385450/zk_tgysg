package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author shallwetalk
 * @Date 2024/2/27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_notice_read")
@EqualsAndHashCode(callSuper = false)
public class TgNoticeRead implements Serializable {

    @TableId
    private Long id;

    private Long userId;

    private Long noticeId;

    // 2. 通知
    private Integer bizType;

    // 申请id
    private Long applicationId;

    // 审核人id
    private Long auditUserId;

    // 审核人是否已读
    private Integer auditUserHasRead;

    // 1. 审核通过 2. 驳回 3. 出数成功 4. 中间流程审核通过
    private Integer auditType;

    private Integer version;


    // 是否已读 0未读 1已读
    private Integer hasRead;

    private Date createTime;

}
