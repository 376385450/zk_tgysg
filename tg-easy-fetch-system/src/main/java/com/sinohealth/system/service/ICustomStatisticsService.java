package com.sinohealth.system.service;


import com.sinohealth.system.domain.statistic.PieModel;
import com.sinohealth.system.domain.statistic.VerticalModel;

import java.util.Map;

public interface ICustomStatisticsService {

    PieModel getResourceLayoutModel(Map<String, Object> parameters);

    VerticalModel getResourceTypeModel(Map<String, Object> parameters);

    VerticalModel getUserTypeModel(Map<String, Object> parameters);
}
