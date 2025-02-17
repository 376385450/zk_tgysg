package com.sinohealth.system.domain.value.deliver.datasource;

import cn.hutool.core.lang.Assert;
import com.sinohealth.system.biz.dataassets.dto.request.DataPreviewRequest;
import com.sinohealth.system.domain.value.deliver.DataSource;
import com.sinohealth.system.domain.value.deliver.DeliverDataSourceType;
import lombok.Getter;


/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 17:16
 */
@Getter
public class ApplicationDataSource implements DataSource {

    private final Long assetsId;

    private String name;

    private DataPreviewRequest requestDTO;

    public ApplicationDataSource(Long assetsId) {
        Assert.isTrue(assetsId != null, "assetsId不能为空");
        this.assetsId = assetsId;
        this.requestDTO = new DataPreviewRequest();
    }


    @Override
    public Long getId() {
        return assetsId;
    }

    @Override
    public DeliverDataSourceType support() {
        return DeliverDataSourceType.ASSETS;
    }

    public ApplicationDataSource setName(String name) {
        this.name = name;
        return this;
    }

    public ApplicationDataSource setRequestDTO(DataPreviewRequest requestDTO) {
        this.requestDTO = requestDTO;
        return this;
    }

}
