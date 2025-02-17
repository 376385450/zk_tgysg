package com.sinohealth.system.dto.notice;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2024/2/27
 */
@Data
public class NoticeReadDTO implements Serializable {

    private List<String> ids;

    private Integer type;

    private List<Integer> types;

    private Date queryTime;

}
