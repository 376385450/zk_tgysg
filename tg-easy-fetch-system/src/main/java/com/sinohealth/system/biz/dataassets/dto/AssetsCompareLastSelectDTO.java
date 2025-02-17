package com.sinohealth.system.biz.dataassets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * @author Kuangcp
 * 2024-07-17 15:09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetsCompareLastSelectDTO {

    public static final AssetsCompareLastSelectDTO empty = new AssetsCompareLastSelectDTO(Collections.emptyList(), Collections.emptyList());
    private List<Long> autoIds;
    private List<Long> handleIds;

}
