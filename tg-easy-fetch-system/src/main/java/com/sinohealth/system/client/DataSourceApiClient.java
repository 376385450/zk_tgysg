package com.sinohealth.system.client;

import com.sinohealth.data.intelligence.datasource.api.DataSourceApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-08 18:20
 */
@FeignClient(contextId = "datasource", name = "intelligence-metadata-service", path = "metadata-service", url = "${sinohealth.metadata-service.url:}")
public interface DataSourceApiClient extends DataSourceApi {
}
