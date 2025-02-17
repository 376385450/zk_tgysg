package com.sinohealth.system.domain.value.deliver.datasource;

import cn.hutool.core.lang.Assert;
import com.sinohealth.system.domain.value.deliver.DataSource;
import com.sinohealth.system.domain.value.deliver.DeliverDataSourceType;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import lombok.Getter;


/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 17:16
 */
@Getter
public class CustomerApplyDataSource implements DataSource {

    private final Long assetsId;

    private String name;

    private GetDataInfoRequestDTO requestDTO;

    public CustomerApplyDataSource(Long assetsId) {
        Assert.isTrue(assetsId != null, "applyId不能为空");
        this.assetsId = assetsId;
        this.requestDTO = new GetDataInfoRequestDTO();
    }


    @Override
    public Long getId() {
        return assetsId;
    }

    @Override
    public DeliverDataSourceType support() {
        return DeliverDataSourceType.CUSTOMER_APPLY;
    }

    public CustomerApplyDataSource setName(String name) {
        this.name = name;
        return this;
    }

    public CustomerApplyDataSource setRequestDTO(GetDataInfoRequestDTO requestDTO) {
        this.requestDTO = requestDTO;
        return this;
    }

}
