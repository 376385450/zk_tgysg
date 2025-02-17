package com.sinohealth.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-30 17:12
 */
@Data
@JsonNaming
public class DeliverEmailTemplateDTO implements Serializable {

    private Integer id;

    private String title;

    private List<String> receiveMails;

    private String content;
}
