package com.sinohealth.system.domain.notice;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author shallwetalk
 * @Date 2024/2/23
 */
@Data
@TableName("tg_notice_relate_asset")
public class TgNoticeRelateAsset {

    @TableId
    private Long id;

    private Long noticeId;

    private Long assetId;

}
