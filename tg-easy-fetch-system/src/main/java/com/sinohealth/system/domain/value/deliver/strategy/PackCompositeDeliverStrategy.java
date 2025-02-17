package com.sinohealth.system.domain.value.deliver.strategy;

import cn.hutool.core.util.ZipUtil;
import com.sinohealth.system.domain.value.deliver.DeliverRequestContextHolder;
import com.sinohealth.system.domain.value.deliver.DiskFile;
import com.sinohealth.system.domain.value.deliver.Resource;
import com.sinohealth.system.domain.value.deliver.resource.ZipResource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 20:31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PackCompositeDeliverStrategy {

    private final CompositeDeliverStrategy delegate;

    public ZipResource deliver(DeliverRequestContextHolder requestContextHolder) throws Exception {
        List<Resource> deliver = delegate.deliver(requestContextHolder.getDataSources(), requestContextHolder.getType());
        // 打包
        DiskFile zipDiskFile = DiskFile.createTmpFile(requestContextHolder.getPackName() + ".zip");
        String[] paths = new String[deliver.size()];
        InputStream[] ins = new InputStream[deliver.size()];
        for (int i = 0; i < deliver.size(); i++) {
            Resource resource = deliver.get(i);
            paths[i] = resource.getName();
            ins[i] = resource.getInputStream();
        }
        ZipUtil.zip(zipDiskFile.getFile(), paths, ins);
        return new ZipResource(zipDiskFile);
    }

}
