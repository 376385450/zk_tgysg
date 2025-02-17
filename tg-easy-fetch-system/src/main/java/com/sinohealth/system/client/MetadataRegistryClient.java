package com.sinohealth.system.client;

import com.sinohealth.data.intelligence.api.metadataRegister.api.MetadataRegisterApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Author shallwetalk
 * @Date 2023/8/21
 */
@FeignClient(contextId = "metadataRegistry", name = "intelligence-metadata-service", path ="metadata-service", url = "${sinohealth.metadata-service.url:}")
public interface MetadataRegistryClient extends MetadataRegisterApi {
}
