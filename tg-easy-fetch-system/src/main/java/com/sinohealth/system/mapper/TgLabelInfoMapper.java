package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.system.domain.label.TgLabelInfo;
import com.sinohealth.system.dto.label.PageQueryLabelRequest;
import com.sinohealth.system.vo.TgLabelInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/18 16:52
 */
@Mapper
public interface TgLabelInfoMapper extends BaseMapper<TgLabelInfo> {

    /**
     * 分页查询
     *
     * @param page
     * @param queryLabelRequest
     * @return
     */
    IPage<TgLabelInfoVo> pageQuery(IPage page, @Param("pageRequest") PageQueryLabelRequest queryLabelRequest);
}
