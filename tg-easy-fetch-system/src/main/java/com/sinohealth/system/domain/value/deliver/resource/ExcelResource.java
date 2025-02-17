package com.sinohealth.system.domain.value.deliver.resource;

import com.sinohealth.system.domain.value.deliver.DeliverResourceType;
import com.sinohealth.system.domain.value.deliver.DiskFile;
import com.sinohealth.system.domain.value.deliver.SupportResourceType;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 17:47
 */
@SupportResourceType(DeliverResourceType.EXCEL)
public class ExcelResource extends AbstractFileResource {

    public ExcelResource(DiskFile diskFile) {
        super(diskFile);
    }
}
