package com.sinohealth.web.controller.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.data.common.response.PageInfo;
import com.sinohealth.system.biz.dict.domain.BizDataDictVal;
import com.sinohealth.system.biz.dict.mapper.BizDataDictValMapper;
import com.sinohealth.system.dto.customer.PageQueryCustomer;
import com.sinohealth.system.dto.customer.SaveCustomerReq;
import com.sinohealth.system.service.CustomerService;
import com.sinohealth.system.vo.CustomerVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2024/1/10
 */
@Api(tags = {"新-客户管理"})
@RestController
@RequestMapping({"/api/customer"})
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @ApiOperation("客户分页")
    @PostMapping("/page")
    public AjaxResult<PageInfo<CustomerVO>> page(@RequestBody PageQueryCustomer page){
        return AjaxResult.success(customerService.page(page));
    }

    @ApiOperation("保存客户")
    @PostMapping("/save")
    public AjaxResult save(@RequestBody @Validated SaveCustomerReq req) {
        customerService.save(req);
        return AjaxResult.success();
    }


    @Autowired
    AppProperties appProperties;

    @Autowired
    BizDataDictValMapper bizDataDictValMapper;

    @GetMapping("/sync/customer")
    @Transactional(rollbackFor = Exception.class)
    public String syncCustomer() {
        final Long customerId = appProperties.getCustomerId();
        final LambdaQueryWrapper<BizDataDictVal> eq = Wrappers.<BizDataDictVal>lambdaQuery()
                .eq(BizDataDictVal::getDictId, customerId);
        final List<BizDataDictVal> bizDataDictVals = bizDataDictValMapper.selectList(eq);
        for (BizDataDictVal bizDataDictVal : bizDataDictVals) {
            final SaveCustomerReq req = new SaveCustomerReq();
            req.setFullName(bizDataDictVal.getVal());
            req.setShortName(bizDataDictVal.getVal());
            req.setCustomerStatus(1);
            customerService.save(req);
        }
        return "OK";
    }

}
