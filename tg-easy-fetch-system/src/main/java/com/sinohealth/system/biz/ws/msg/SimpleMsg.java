package com.sinohealth.system.biz.ws.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-02-29 11:02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleMsg implements IMsg {
    private Long userId;
    private String type;
}
