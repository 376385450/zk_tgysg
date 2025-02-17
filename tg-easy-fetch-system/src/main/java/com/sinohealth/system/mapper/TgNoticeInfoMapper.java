package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.system.domain.notice.TgNoticeInfo;
import com.sinohealth.system.dto.notice.PageQueryNoticeRequest;
import com.sinohealth.system.vo.TgNoticeInfoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/23 14:26
 */
@Mapper
public interface TgNoticeInfoMapper extends BaseMapper<TgNoticeInfo> {

    /**
     * 分页查询
     *
     * @param page
     * @param pageRequest
     * @return
     */
    IPage<TgNoticeInfoVo> pageQuery(Page page, @Param("request") PageQueryNoticeRequest pageRequest);
}
