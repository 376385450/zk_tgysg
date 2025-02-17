package com.sinohealth.system.biz.dir.dto;

import com.sinohealth.common.utils.dto.Node;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.sinohealth.common.constant.CommonConstants.ICON_FILE_ASSETS;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-17 10:51
 */
@Data
public class UserFileAssetsDirItemDTO implements Node<UserFileAssetsDirItemDTO> {

    private Long id;

    @ApiModelProperty("资产ID")
    private Long assetsId;

    private String icon = ICON_FILE_ASSETS;

    private String dirName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("目录id")
    private Long parentId;

    @ApiModelProperty("序号")
    private Integer sort;

    private Integer moved;

    @ApiModelProperty("子节点")
    private List<UserFileAssetsDirItemDTO> children = new ArrayList<>();

    @Override
    public String getNodeViewName() {
        return dirName;
    }

    @Override
    public Date getLastUpdate() {
        return null;
    }

    @Override
    public Integer getRequireTimeType() {
        return null;
    }

    @Override
    public Integer getRequireAttr() {
        return null;
    }

    @Override
    public String getClientNames() {
        return null;
    }
}
