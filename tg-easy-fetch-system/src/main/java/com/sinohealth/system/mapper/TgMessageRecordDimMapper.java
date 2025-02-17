package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.TgMessageRecordDim;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface TgMessageRecordDimMapper extends BaseMapper<TgMessageRecordDim>  {
    TgMessageRecordDim queryOneMessageByPushed(@Param("uid")String uid);

    List<TgMessageRecordDim> queryMessageListByPushed(@Param("uid")String uid);

    List<TgMessageRecordDim> queryMessageListByViewed(@Param("uid")String uid);

    Integer queryMessageCountByViewed(@Param("uid")String uid);

    void updateMessageCountByApplicationIdAndAdviceWho(@Param("uid") Long uid, @Param("applicationId") Long applicationId);

    void markMsgAllRead(@Param("uid") String uid);
}
