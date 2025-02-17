package com.sinohealth.system.domain.value.deliver.resource;

import com.sinohealth.system.domain.value.deliver.DeliverResourceType;
import com.sinohealth.system.domain.value.deliver.DiskFile;
import com.sinohealth.system.domain.value.deliver.SupportResourceType;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-25 13:48
 */
@SupportResourceType(DeliverResourceType.CSV)
public class CsvResource extends AbstractFileResource {

    public CsvResource(DiskFile diskFile) {
        super(diskFile);
    }
}
