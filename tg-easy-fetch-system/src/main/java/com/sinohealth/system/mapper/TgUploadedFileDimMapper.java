package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.TgUploadedFileDim;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface TgUploadedFileDimMapper extends BaseMapper<TgUploadedFileDim> {

    List<TgUploadedFileDim> listTasks(@Param("maxRetry") Integer maxRetry);

}
