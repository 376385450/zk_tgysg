package com.sinohealth.system.biz.dataassets.dto;

import com.sinohealth.common.enums.dataassets.AssetsSnapshotTypeEnum;
import com.sinohealth.system.domain.TgApplicationInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-16 16:51
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertAssetsBO {

    private TgApplicationInfo info;

    private String tableName;
    /**
     * @see AssetsSnapshotTypeEnum
     */
    private String snapshotType;

    private Long triggerId;

    private boolean file;

    private String filePath;

    public LocalDateTime calcDataExpire() {
        return Objects.isNull(info.getDataExpir()) ? null
                : LocalDateTime.ofInstant(info.getDataExpir().toInstant(), ZoneOffset.systemDefault());
    }

    public String calcAssetsSql() {
        boolean apply = Objects.isNull(tableName);
        return apply ? info.getAsql() : "SELECT * FROM " + tableName;
    }
}
