package com.sinohealth.system.dto;

import com.sinohealth.system.domain.vo.FastEntryVO;
import lombok.Data;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-04-23 13:56
 */
@Data
public class FastEntryUpsertRequest {

    private List<FastEntryVO> list;
}
