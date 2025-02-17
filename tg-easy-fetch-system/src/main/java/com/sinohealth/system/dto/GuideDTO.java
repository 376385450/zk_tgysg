package com.sinohealth.system.dto;

import com.sinohealth.system.domain.AssetLink;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2024/3/15
 */
@Data
public class GuideDTO implements Serializable {

    private Integer guide;

    private String guideDesc;

    private List<AssetLink> guideLinks;

}
