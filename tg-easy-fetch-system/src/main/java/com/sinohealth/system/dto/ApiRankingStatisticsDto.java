package com.sinohealth.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ApiRankingStatisticsDto implements Serializable {

    List<ApiRankingDto> apiInvokeRankingDtoList;

    List<ApiRankingDto> apiSubscribeRankingDtoList;

}
