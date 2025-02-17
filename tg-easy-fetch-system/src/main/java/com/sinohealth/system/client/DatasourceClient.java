package com.sinohealth.system.client;

import com.sinohealth.data.intelligence.api.datasource.api.DatasourceApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author shallwetalk
 * @Date 2023/11/6
 */
@FeignClient(contextId = "datasource", name = "intelligence-metadata-service", path = "metadata-service", url = "${sinohealth.metadata-service.url:}")
public interface DatasourceClient extends DatasourceApi {
}
