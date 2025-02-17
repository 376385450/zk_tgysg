package com.sinohealth.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sinohealth.common.utils.bean.BeanUtils;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2023-03-10 18:26
 */
@Data
@JsonNaming
@ApiModel("编辑地图目录请求参数")
public class DataDirUpdateReqDTO implements Serializable {

    @Valid
    @NotEmpty(message = "请至少创建一个节点")
    private List<DataDirVO> list;

    public List<DataDirDto> flat(List<DataDirDto> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<DataDirDto> result = new ArrayList<>();
        for (DataDirDto dataDirDto : list) {
            result.add(dataDirDto);
            result.addAll(flat(dataDirDto.getChildren()));
        }
        return result;
    }

    public static List<DataDirDto> getDtoTree(List<DataDirVO> list, Long parentId) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<DataDirDto> result = new ArrayList<>();
        for (DataDirVO dataDirVO : list) {
            DataDirDto dataDir = new DataDirDto();
            BeanUtils.copyProperties(dataDirVO, dataDir);
            // 根据isNewNode去判断是否是新增节点，别问我为什么不用id，因为前端不行
            if (BooleanUtils.isTrue(dataDirVO.getIsNewNode())) {
                dataDir.setId(null);
            } else {
                dataDir.setId(Long.valueOf(dataDirVO.getId()));
            }
            dataDir.setParentId(parentId);
            result.add(dataDir);
            dataDir.setChildren(getDtoTree(dataDirVO.getChildren(), dataDir.getId()));
        }
        return result;
    }


}
