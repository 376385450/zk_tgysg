package com.sinohealth.system.service;

import com.sinohealth.system.dto.CustomerApplyAuthBatchUpdateReqDTO;
import com.sinohealth.system.dto.CustomerApplyAuthList;
import com.sinohealth.system.dto.CustomerApplyAuthListReqDTO;
import com.sinohealth.system.dto.CustomerApplyAuthUpdateReqV2DTO;
import com.sinohealth.system.dto.assets.SubCustomerAssetsBatchUpdateReqDTO;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-13 13:51
 */
public interface CustomerAuthV2Service {

    /**
     * 编辑客户资产权限
     * @param reqV2DTO
     */
    void batchUpdate(CustomerApplyAuthUpdateReqV2DTO reqV2DTO);

    void batchUpdate(CustomerApplyAuthBatchUpdateReqDTO reqDTO);

    /**
     * 编辑子账号资产
     * @param reqDTO
     */
    void batchUpdateSub(SubCustomerAssetsBatchUpdateReqDTO reqDTO);

    /**
     * 返回用户已授权的资产，如果是提数申请需要查询出关联的所有图标分析
     * @param reqDTO
     * @return
     */
    CustomerApplyAuthList queryAuthList(CustomerApplyAuthListReqDTO reqDTO);

}
