package com.sinohealth.system.biz.dir.bo;

import com.sinohealth.system.domain.TgDocInfo;
import com.sinohealth.system.dto.DocDataDirItemDto;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-01-16 19:27
 */
@Data
@AllArgsConstructor
public class DocItemBO {
    private TgDocInfo docInfo;
    private DocDataDirItemDto treeNode;
}
