package com.sinohealth.system.biz.ws.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-03-01 10:46
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleIdMsg implements IMsg {
    private Long userId;
    private String type;
    private Long id;
}
