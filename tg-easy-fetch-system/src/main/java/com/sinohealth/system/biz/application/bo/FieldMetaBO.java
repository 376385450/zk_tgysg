package com.sinohealth.system.biz.application.bo;

import com.sinohealth.system.domain.CustomFieldInfo;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.domain.TgApplicationInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-02-08 20:49
 */
@Data
@AllArgsConstructor
public class FieldMetaBO {

    // 中间值
    private TgApplicationInfo applyInfo;
    private List<TableFieldInfo> tableFields;
    private List<CustomFieldInfo> customFields;

    public List<Object> mergeFields() {
        return new LinkedList<Object>() {{
            addAll(tableFields);
            addAll(customFields);
        }};
    }

    public FieldMetaBO(List<TableFieldInfo> tableFields, List<CustomFieldInfo> customFields) {
        this.tableFields = tableFields;
        this.customFields = customFields;
    }

    private TableFieldInfo buildField(String filedName, String filedAlias) {
        TableFieldInfo info = new TableFieldInfo();
        info.setId(0L);
        info.setFieldName(filedName);
        info.setFieldAlias(filedAlias);
        info.setComment(filedAlias);
        info.setRealName(filedAlias);
        info.setDataType("String");
        return info;
    }

    /**
     * TODO 追加日期字段
     */
    public List<Object> mergeFieldsWithPeriod() {
        return mergeFields();
//        List<Object> result = mergeFields();
//        if (StringUtils.isNoneBlank(applyInfo.getPeriodField())) {
//            if (CollectionUtils.isNotEmpty(applyInfo.getColsInfo())) {
//                List<Long> selectPeriod = applyInfo.getColsInfo().stream().flatMap(v -> v.getSelect().stream())
//                        .filter(ApplicationConst.PeriodField.idToNameMap::containsKey).collect(Collectors.toList());
//
//                if (selectPeriod.size() != 0 && selectPeriod.size() != ApplicationConst.PeriodField.idToNameMap.size()) {
//                    for (Long id : selectPeriod) {
//                        ApplicationConst.PeriodFieldEnum of = ApplicationConst.PeriodFieldEnum.of(id);
//                        result.add(buildField(of.getName(), of.getAlias()));
//                    }
//                } else {
//                    ApplicationConst.PeriodField.idToNameMap.forEach((id, v) -> {
//                        ApplicationConst.PeriodFieldEnum of = ApplicationConst.PeriodFieldEnum.of(id);
//                        result.add(buildField(of.getName(), of.getAlias()));
//                    });
//                }
//            }
//        }
//        return result;
    }

    /**
     * 排除旧模板的指标 如果做版本的精细管理会更准确
     */
    public List<Object> mergeFieldsExcludeDelete() {
        return new LinkedList<Object>() {{
            addAll(tableFields);
            if (CollectionUtils.isNotEmpty(customFields)) {
                customFields.stream().filter(v -> BooleanUtils.isNotTrue(v.getHiddenForApply())).forEach(this::add);
            }
        }};
    }

    public List<CustomFieldInfo> getCustomExcludeDelete() {
        return customFields.stream().filter(v -> BooleanUtils.isNotTrue(v.getHiddenForApply())).collect(Collectors.toList());
    }

    public boolean isAllEmpty() {
        return CollectionUtils.isEmpty(tableFields) && CollectionUtils.isEmpty(customFields);
    }

}
