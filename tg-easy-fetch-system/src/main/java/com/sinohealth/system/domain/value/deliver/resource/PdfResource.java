package com.sinohealth.system.domain.value.deliver.resource;

import com.sinohealth.system.domain.value.deliver.DeliverResourceType;
import com.sinohealth.system.domain.value.deliver.DiskFile;
import com.sinohealth.system.domain.value.deliver.SupportResourceType;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 20:13
 */
@SupportResourceType(DeliverResourceType.PDF)
public class PdfResource extends AbstractFileResource {

    public PdfResource(DiskFile diskFile) {
        super(diskFile);
    }

    @Override
    public InputStream getInputStream() throws Exception {
        return new FileInputStream(diskFile.getFile());
    }

    @Override
    public String getName() {
        return diskFile.getFile().getName();
    }
}
