package com.sinohealth.system.biz.dir.vo;

import com.sinohealth.common.core.domain.entity.DataDir;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-02-24 17:58
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataDirListVO {

    private List<DataDir> dirs;

    /**
     * 存在一级目录外的文件
     */
    private Boolean fileOutOfDir;

    public static final DataDirListVO EMPTY =  new DataDirListVO(Collections.emptyList(), false);
}
