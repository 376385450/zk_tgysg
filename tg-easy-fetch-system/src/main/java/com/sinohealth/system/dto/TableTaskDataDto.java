package com.sinohealth.system.dto;

import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TableTask;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TableTaskDataDto {

    private String copytable;

    private TableTask tableTask;
    private TableInfo tableInfo;

}
