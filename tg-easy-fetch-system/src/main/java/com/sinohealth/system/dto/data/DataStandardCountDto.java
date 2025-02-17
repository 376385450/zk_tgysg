package com.sinohealth.system.dto.data;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Jingjun
 * @since 2021/5/28
 */
@Data
@ApiModel("DataStandardCountDto")
@AllArgsConstructor
@NoArgsConstructor
public class DataStandardCountDto {

    private long totalUpdate;
    private long lastUpdate;
}
