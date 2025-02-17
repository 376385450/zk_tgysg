package com.sinohealth.system.client;

import com.sinohealth.data.intelligence.api.metadata.api.MetadataApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author shallwetalk
 * @Date 2023/8/21
 */
@FeignClient(contextId = "metadata", name = "intelligence-metadata-service", path = "metadata-service", url = "${sinohealth.metadata-service.url:}")
public interface MetadataClient extends MetadataApi {
}
