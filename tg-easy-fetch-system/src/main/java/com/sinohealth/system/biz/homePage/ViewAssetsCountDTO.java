package com.sinohealth.system.biz.homePage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author shallwetalk
 * @Date 2023/8/26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewAssetsCountDTO {

    private Integer assetId;

    private Integer count;

}
