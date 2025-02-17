package com.sinohealth.web.controller.label;

import com.github.pagehelper.PageInfo;
import com.sinohealth.api.label.LabelApi;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.dto.label.AddLabelRequest;
import com.sinohealth.system.dto.label.DeleteLabelRequest;
import com.sinohealth.system.dto.label.PageQueryLabelRequest;
import com.sinohealth.system.dto.label.UpdateLabelRequest;
import com.sinohealth.system.service.ILabelService;
import com.sinohealth.system.vo.TgLabelInfoVo;
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
 * @Date 2023/8/18 16:05
 */
@RestController
@RequestMapping("/api/label")
@Slf4j
@Api(tags = "标签管理")
public class LabelApiController extends BaseController implements LabelApi {

    @Resource
    private ILabelService labelService;

    /**
     * 分页查询
     *
     * @param queryLabelRequest
     * @return
     */
    @ApiOperation("分页查询标签")
    @PostMapping("/pageQuery")
    public AjaxResult<PageInfo<TgLabelInfoVo>> pageQuery(@RequestBody @Validated PageQueryLabelRequest queryLabelRequest) throws InterruptedException {
        return labelService.pageQuery(queryLabelRequest);
    }

    /**
     * 新增标签
     *
     * @param addLabelRequest
     * @return
     */
    @ApiOperation("新增标签")
    @PostMapping("/addLabel")
    public AjaxResult<Object> addLabel(@RequestBody @Validated AddLabelRequest addLabelRequest) {
        return labelService.addLabel(addLabelRequest);
    }

    /**
     * 更新标签
     *
     * @param updateLabelRequest
     * @return
     */
    @ApiOperation("更新标签")
    @PostMapping("/updateLabel")
    public AjaxResult<Object> updateLabel(@RequestBody @Validated UpdateLabelRequest updateLabelRequest) {
        return labelService.updateLabel(updateLabelRequest);
    }

    /**
     * 删除标签
     *
     * @param deleteLabelRequest
     * @return
     */
    @ApiOperation("删除标签")
    @PostMapping("/deleteLabel")
    public AjaxResult<Object> deleteLabel(@RequestBody @Validated DeleteLabelRequest deleteLabelRequest) {
        return labelService.deleteLabel(deleteLabelRequest);
    }
}
