package com.sinohealth.system.domain.value.deliver.datasource;

import cn.hutool.core.lang.Assert;
import com.sinohealth.system.domain.value.deliver.DataSource;
import com.sinohealth.system.domain.value.deliver.DeliverDataSourceType;
import lombok.Getter;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 20:23
 */
@Getter
public class PanelDataSource implements DataSource {

    private Long arkbiId;

    private String name;

    public PanelDataSource(Long arkbiId, String name) {
        Assert.isTrue(arkbiId != null, "arkbId不能为空");
        this.arkbiId = arkbiId;
        this.name = name;
    }

    @Override
    public Long getId() {
        return arkbiId;
    }

    @Override
    public DeliverDataSourceType support() {
        return DeliverDataSourceType.PANEL;
    }

}
