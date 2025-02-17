package com.sinohealth.system.domain.vo;

import com.sinohealth.system.domain.TableInfoDiy;
import lombok.Data;

@Data
public class DiyTableUpdateResult {
    private TableInfoDiy data;
    private Integer code;
    private String msg;

    public DiyTableUpdateResult() {
    }

    public DiyTableUpdateResult(TableInfoDiy data) {
        this.data = data;
    }

    public DiyTableUpdateResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
