package com.sinohealth.system.domain;

import com.sinohealth.system.dto.table_manage.TableInfoManageDto;
import lombok.Data;

/**
 * @Author Rudolph
 * @Date 2023-08-09 11:45
 * @Desc
 */
@Data
public class TgAssetTableInfo {

    private TgAssetInfo tgAssetInfo;
    private TableInfoManageDto bindingData;
    private TgMetadataInfo tgMetadataInfo;
}
