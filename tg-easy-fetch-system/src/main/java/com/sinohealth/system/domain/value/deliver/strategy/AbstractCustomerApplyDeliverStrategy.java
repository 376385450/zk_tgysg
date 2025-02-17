package com.sinohealth.system.domain.value.deliver.strategy;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.sinohealth.system.dao.TgTableApplicationMappingInfoDAO;
import com.sinohealth.system.domain.ckpg.CustomerCKProperties;
import com.sinohealth.system.domain.value.deliver.DataSource;
import com.sinohealth.system.domain.value.deliver.Resource;
import com.sinohealth.system.domain.value.deliver.ResourceDeliverStrategy;
import com.sinohealth.system.domain.value.deliver.util.HiddenFieldUtils;
import com.sinohealth.system.domain.vo.TgTableApplicationMappingInfo;
import com.sinohealth.system.dto.assets.AuthTableFieldDTO;
import com.sinohealth.system.mapper.OutsideClickhouseMapper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-04-26 10:22
 */
public abstract class AbstractCustomerApplyDeliverStrategy<T extends DataSource, R extends Resource>
        implements ResourceDeliverStrategy<T, R> {

    abstract TgTableApplicationMappingInfoDAO getTgTableApplicationMappingInfoDAO();

    abstract OutsideClickhouseMapper getOutsideClickhouseMapper();

    abstract CustomerCKProperties getCustomerCKProperties();

    /**
     * @return 别名数组 字段名数组
     */
    public Pair<String[], String[]> getHeaders(Long assetsId) {
        TgTableApplicationMappingInfo mappingInfo = getTgTableApplicationMappingInfoDAO().getByAssetsId(assetsId);
        if (Objects.isNull(mappingInfo)) {
            throw new RuntimeException("数据同步失败");
        }
        List<AuthTableFieldDTO> fields = getOutsideClickhouseMapper().getFields(getCustomerCKProperties().getDatabase(),
                mappingInfo.getDataTableName());
        String[] aliasHeader = fields.stream()
                .filter(HiddenFieldUtils.CUSTOMER_PREDICATE)
                .map(a -> ObjectUtils.isNotNull(a.getFieldAlias()) ? a.getFieldAlias() : a.getFieldName())
                .toArray(String[]::new);
        String[] nameHeader = fields.stream()
                .filter(HiddenFieldUtils.CUSTOMER_PREDICATE)
                .map(AuthTableFieldDTO::getFieldName)
                .toArray(String[]::new);
        return Pair.of(aliasHeader, nameHeader);
    }


}
