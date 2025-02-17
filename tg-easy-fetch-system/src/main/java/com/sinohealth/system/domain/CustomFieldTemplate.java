package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.system.dto.application.CustomFieldDepFieldVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-01-30 13:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_custom_field_template")
@Accessors(chain = true)
public class CustomFieldTemplate extends Model<CustomFieldTemplate> {

    /**
     * @see CommonConstants.ComputeWay
     */
    @ApiModelProperty("主键自增")
    private Integer id;

    @ApiModelProperty("基础表ID")
    @NotNull
    private Long baseTableId;

    private String showName;
    private String comment;

    private String templateDep;

    private String apply;

    public List<CustomFieldDepFieldVO> parseTemplateDep() {
        return JsonUtils.parse(templateDep, new TypeReference<List<CustomFieldDepFieldVO>>() {
        });
    }
}
