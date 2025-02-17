package com.sinohealth.system.biz.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.dict.domain.ProjectCustomFieldDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-18 19:41
 */
@Mapper
@DataSource(DataSourceType.MASTER)
public interface ProjectCustomFieldDictMapper extends BaseMapper<ProjectCustomFieldDict> {

    @Select("select application_id, max(update_time) as update_time from tg_project_custom_field_dict " +
            "where project_id = #{projectId} and biz_type = #{bizType} " +
            "group by application_id order by update_time desc limit 1")
    ProjectCustomFieldDict queryLatestApply(@Param("projectId") Long projectId,
                                            @Param("bizType") String bizType);

}
