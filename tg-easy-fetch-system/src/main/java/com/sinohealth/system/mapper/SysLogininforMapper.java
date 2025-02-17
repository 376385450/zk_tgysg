package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.SysLogininfor;
import com.sinohealth.system.dto.DataStatisticsDTO;
import com.sinohealth.system.dto.TgLoginInfoDTO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 系统访问日志情况信息 数据层
 *
 * @author dataplatform
 */
public interface SysLogininforMapper extends BaseMapper<SysLogininfor> {

    /**
     * 查询用户的活跃统计
     */
    @MapKey("org_user_id")
    Map<String, Map<String, Integer>> countByOrgUserId(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * @param dateFormat 按日 '%Y-%m-%d' ,按月 '%Y-%m'
     */
    List<DataStatisticsDTO> countByDayOrMonth(@Param("dateFormat") String dateFormat, @Param("startTime") Date startTime, @Param("endTime") Date endTime);


    List<DataStatisticsDTO> countDepByDayOrMonth(@Param("dateFormat") String dateFormat, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<TgLoginInfoDTO> queryLoginInfoByPage(@Param("startTime") Date startTime,
                                              @Param("endTime") Date endTime,
                                              @Param("nameSearch") String nameSearch,
                                              @Param("userIds") Set<Long> userIds
    );

    Integer countDua(@Param("startTime") Date startTime,
                     @Param("endTime") Date endTime);

}
