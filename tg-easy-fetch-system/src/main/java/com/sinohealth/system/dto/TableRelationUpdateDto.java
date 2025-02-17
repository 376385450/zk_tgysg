package com.sinohealth.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author Jingjun
 * @since 2021/4/24
 */
@Data
@ApiModel("TableRelationUpdateDto")
public class TableRelationUpdateDto {

    @JsonIgnore
    private Long tableId;

    @NotEmpty
    private List<TableRelationDto> list;
}
