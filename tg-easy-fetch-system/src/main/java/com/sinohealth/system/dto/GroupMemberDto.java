package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Jingjun
 * @since 2021/4/27
 */
@Data
@ApiModel("GroupMemberDto")
public class GroupMemberDto {
    @NotNull
    Long groupId;
    @NotNull
    Boolean isAdd;
    @NotEmpty
    List<Long> userIds;
}
