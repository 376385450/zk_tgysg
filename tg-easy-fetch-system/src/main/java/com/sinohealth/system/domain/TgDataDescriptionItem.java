package com.sinohealth.system.domain;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-07 14:13
 */
@Data
@JsonNaming
@Accessors(chain = true)
public class TgDataDescriptionItem {

    private List<TgDataDescriptionQuota> list;

}
