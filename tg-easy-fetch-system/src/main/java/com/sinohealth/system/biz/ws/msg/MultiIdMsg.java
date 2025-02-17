package com.sinohealth.system.biz.ws.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-03-01 10:51
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiIdMsg implements IMsg {
    private Long userId;
    private String type;
    private List<Long> id;
}
