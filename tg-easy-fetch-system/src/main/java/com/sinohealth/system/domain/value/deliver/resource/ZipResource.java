package com.sinohealth.system.domain.value.deliver.resource;

import com.sinohealth.system.domain.value.deliver.DeliverResourceType;
import com.sinohealth.system.domain.value.deliver.DiskFile;
import com.sinohealth.system.domain.value.deliver.SupportResourceType;


/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-29 14:11
 */
@SupportResourceType(DeliverResourceType.ZIP)
public class ZipResource extends AbstractFileResource {

    public ZipResource(DiskFile diskFile) {
        super(diskFile);
    }

}
