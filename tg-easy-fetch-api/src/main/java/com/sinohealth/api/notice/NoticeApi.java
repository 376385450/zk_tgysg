package com.sinohealth.api.notice;

import com.github.pagehelper.PageInfo;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.dto.notice.*;
import com.sinohealth.system.vo.TgNoticeInfoVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/23 14:12
 */
@RequestMapping("/api/notice")
public interface NoticeApi {

    @ApiOperation("分页查询")
    @PostMapping("/pageQuery")
    AjaxResult<PageInfo<TgNoticeInfoVo>> pageQuery(@RequestBody @Validated PageQueryNoticeRequest pageQueryNoticeRequest);

    @ApiOperation("新增公告")
    @PostMapping("/add")
    AjaxResult<Object> add(@RequestBody @Validated AddNoticeRequest addRequest);

    @ApiOperation("更新公告")
    @PostMapping("/update")
    AjaxResult<Object> update(@RequestBody @Validated UpdateNoticeRequest updateRequest);

    @ApiOperation("更新公告的置顶状态")
    @PostMapping("/updateIsTop")
    AjaxResult<Object> updateIsTop(@RequestBody @Validated UpdateNoticeIsTopRequest updateTopRequest);

    @ApiOperation("删除公告")
    @PostMapping("/delete")
    AjaxResult<Object> delete(@RequestBody @Validated DeleteNoticeRequest deleteNotice);
}
