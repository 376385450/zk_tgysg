package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.domain.TgCustomerApplyAuth;
import com.sinohealth.system.dto.CustomerApplyDTO;
import com.sinohealth.system.dto.CustomerAuthAlreadyApplyQuery;
import com.sinohealth.system.dto.TgCustomerApplyAuthDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;


@Mapper
@DataSource(DataSourceType.MASTER)
public interface SysCustomerAuthMapper extends BaseMapper<TgCustomerApplyAuth> {

    void updateByBatchIds(@Param("ids") List<Integer> ids);

    List<TgCustomerApplyAuthDto> getList(@Param("assetsId") Integer assetsId, @Param("userId") Long userId);

    List<TgCustomerApplyAuthDto> getListV2(@Param("dataDirIds") Collection<Long> dataDirIds,
                                           @Param("userId") Long userId, @Param("applyId") Long applyId);

    List<CustomerApplyDTO> listAuth(@Param("dataDirIds") List<Long> dataDirIds, @Param("userId") Long userId,
                                    @Param("assetsId") Long assetsId);

    List<CustomerApplyDTO> listChartAuth(@Param("dataDirIds") List<Long> parentDataDirIds, @Param("userId") Long userId);

    int getCountByUserId(@Param("userId") Long userId, @Param("excludeIcon") String excludeIcon);

    List<TgCustomerApplyAuthDto> getListForApply(TgCustomerApplyAuthDto tgCustomerApplyAuthDto);

    /**
     * 获取已分配资产列表
     */
    List<TgCustomerApplyAuth> getAlreadyApplyList(@Param("query") CustomerAuthAlreadyApplyQuery query,
                                                  @Param("userId") Long userId);

    List<TgCustomerApplyAuth> queryForTree(@Param("userId") Long userId,
                                           @Param("status") Integer status,
                                           @Param("searchKey") String searchKey);
}
