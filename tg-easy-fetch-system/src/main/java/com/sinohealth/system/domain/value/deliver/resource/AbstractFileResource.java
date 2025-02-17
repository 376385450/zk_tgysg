package com.sinohealth.system.domain.value.deliver.resource;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import com.sinohealth.system.domain.value.deliver.DiskFile;
import com.sinohealth.system.domain.value.deliver.Resource;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-29 14:13
 */
public abstract class AbstractFileResource implements Resource {

    protected DiskFile diskFile;

    public AbstractFileResource(DiskFile diskFile) {
        Assert.isTrue(FileUtil.exist(diskFile.getFile()), "文件不存在：" + diskFile.getFile().getName());
        this.diskFile = diskFile;
    }

    @Override
    public InputStream getInputStream() throws Exception {
        return new FileInputStream(diskFile.getFile());
    }

    @Override
    public String getName() {
        return diskFile.getFile().getName();
    }

    @Override
    public void clean() {
        if (Objects.nonNull(diskFile)) {
            diskFile.destroy();
        }
    }
}
