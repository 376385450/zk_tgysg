package com.sinohealth.api.label;

import com.github.pagehelper.PageInfo;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.dto.label.AddLabelRequest;
import com.sinohealth.system.dto.label.DeleteLabelRequest;
import com.sinohealth.system.dto.label.PageQueryLabelRequest;
import com.sinohealth.system.dto.label.UpdateLabelRequest;
import com.sinohealth.system.vo.TgLabelInfoVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/18 16:12
 */
@RequestMapping("/api/label")
public interface LabelApi {

    /**
     * 分页查询
     *
     * @param queryLabelRequest
     * @return
     */
    @PostMapping("/pageQuery")
    public AjaxResult<PageInfo<TgLabelInfoVo>> pageQuery(@RequestBody @Validated PageQueryLabelRequest queryLabelRequest) throws InterruptedException;

    /**
     * 新增标签
     *
     * @param addLabelRequest
     * @return
     */
    @PostMapping("/addLabel")
    public AjaxResult<Object> addLabel(@RequestBody @Validated AddLabelRequest addLabelRequest);

    /**
     * 更新标签
     *
     * @param updateLabelRequest
     * @return
     */
    @PostMapping("/updateLabel")
    public AjaxResult<Object> updateLabel(@RequestBody @Validated UpdateLabelRequest updateLabelRequest);

    /**
     * 删除标签
     *
     * @param deleteLabelRequest
     * @return
     */
    @PostMapping("/deleteLabel")
    public AjaxResult<Object> deleteLabel(@RequestBody @Validated DeleteLabelRequest deleteLabelRequest);
}
