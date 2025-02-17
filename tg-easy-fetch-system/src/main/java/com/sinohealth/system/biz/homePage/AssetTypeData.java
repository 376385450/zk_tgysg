package com.sinohealth.system.biz.homePage;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/9/18
 */
@Data
public class AssetTypeData implements Serializable {

    private String name;

    private List<Integer> data;

}
