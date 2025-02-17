package com.sinohealth.system.biz.ws.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-02-29 14:14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueMsg {

    private Long userId;
    private String msg;
}
