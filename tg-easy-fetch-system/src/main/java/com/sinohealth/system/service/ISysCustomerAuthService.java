package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.domain.TgCustomerApplyAuth;
import com.sinohealth.system.dto.TgCustomerApplyAuthDto;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


public interface ISysCustomerAuthService extends IService<TgCustomerApplyAuth> {

    List<TgCustomerApplyAuthDto> queryList(Integer applyId,Long userId);

    List<TgCustomerApplyAuthDto> queryListV2(Long userId, Long assetsId, List<Long> dataDirIds);

    int getCountByUserId(Long userId);

    int getCountByUserId(Long userId, String excludeIcon);

    List<TgCustomerApplyAuthDto> getListForApply(TgCustomerApplyAuthDto tgCustomerApplyAuthDto);

    AjaxResult<Object> checkParam(Long applyId, String auth, HttpServletResponse response);

    List<TgCustomerApplyAuth> queryForTree(Long userId, Integer status, String searchKey);
}
