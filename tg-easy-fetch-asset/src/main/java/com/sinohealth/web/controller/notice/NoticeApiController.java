package com.sinohealth.web.controller.notice;

import com.github.pagehelper.PageInfo;
import com.sinohealth.api.notice.NoticeApi;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.dto.notice.*;
import com.sinohealth.system.service.INoticeService;
import com.sinohealth.system.vo.TgNoticeInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/23 14:12
 */
@RestController
@RequestMapping("/api/notice")
@Slf4j
@Api(tags = "公告管理")
public class NoticeApiController implements NoticeApi {

    @Resource
    private INoticeService noticeService;

    @Override
    @ApiOperation("分页查询公告")
    @PostMapping("/pageQuery")
    public AjaxResult<PageInfo<TgNoticeInfoVo>> pageQuery(@RequestBody @Validated PageQueryNoticeRequest pageQueryNoticeRequest) {

        return noticeService.pageQuery(pageQueryNoticeRequest);
    }

    @Override
    @ApiOperation("新增公告")
    @PostMapping("/add")
    public AjaxResult<Object> add(@RequestBody @Validated AddNoticeRequest addRequest) {

        return noticeService.add(addRequest);
    }

    @Override
    @ApiOperation("更新公告")
    @PostMapping("/update")
    public AjaxResult<Object> update(@RequestBody @Validated UpdateNoticeRequest updateRequest) {

        return noticeService.update(updateRequest);
    }

    @Override
    @ApiOperation("更新公告的置顶状态")
    @PostMapping("/updateIsTop")
    public AjaxResult<Object> updateIsTop(@RequestBody @Validated UpdateNoticeIsTopRequest updateTopRequest) {

        return noticeService.updateIsTop(updateTopRequest);
    }

    @Override
    @ApiOperation("删除公告")
    @PostMapping("/delete")
    public AjaxResult<Object> delete(@RequestBody @Validated DeleteNoticeRequest deleteNotice) {

        return noticeService.delete(deleteNotice);
    }
}

