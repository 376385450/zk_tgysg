package com.sinohealth.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.PageInfo;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.dto.application.Notice;
import com.sinohealth.system.dto.notice.*;
import com.sinohealth.system.vo.TgNoticeInfoVo;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/23 14:16
 */
public interface INoticeService {

    /**
     * 分页查询
     *
     * @param pageRequest
     * @return
     */
    AjaxResult<PageInfo<TgNoticeInfoVo>> pageQuery(PageQueryNoticeRequest pageRequest);

    /**
     * 新增公告
     *
     * @param addRequest
     * @return
     */
    AjaxResult<Object> add(AddNoticeRequest addRequest);

    /**
     * 更新公告
     *
     * @param updateRequest
     * @return
     */
    AjaxResult<Object> update(UpdateNoticeRequest updateRequest);

    /**
     * 更新公告的置顶状态
     *
     * @param updateTopRequest
     * @return
     */
    AjaxResult<Object> updateIsTop(UpdateNoticeIsTopRequest updateTopRequest);

    /**
     * 删除公告
     *
     * @param deleteNotice
     * @return
     */
    AjaxResult<Object> delete(DeleteNoticeRequest deleteNotice);


    IPage<TgNoticeInfoVo> pageNoticeByType(Long pageNum, Long pageSize, String type, Date queryTime, Collection<Long> id);

    List<TgNoticeInfoVo> getAllNotice(String type, Date queryTime);

}
