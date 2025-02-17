package com.sinohealth.system.service;

import java.util.Date;

public interface ITaskService {

    public void countTableRowByUpdateTime(Date now);

    public void updateUseStatics(Date now);

    public void syncTableInfo();

    void updateTableHeat();

}
