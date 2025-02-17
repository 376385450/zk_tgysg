package com.sinohealth.system.util;

import com.sinohealth.bi.data.Filter;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.domain.vo.TgDataRangeGroupVO;
import com.sinohealth.system.domain.vo.TgDataRangeTemplateVO;
import com.sinohealth.system.domain.vo.TgDataRangeVO;
import com.sinohealth.system.dto.analysis.FilterDTO;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-12 11:26
 */
public class RangeTemplateUtil {

    public static void extractCategoryId(TgDataRangeTemplateVO root, Set<Long> categoryIds) {
        if (root == null) {
            return;
        }
        if (root.getCategoryId() != null) {
            categoryIds.add(root.getCategoryId());
        }
        if (CollectionUtils.isNotEmpty(root.getChildren())) {
            for (TgDataRangeTemplateVO child : root.getChildren()) {
                extractCategoryId(child, categoryIds);
            }
        }
    }

    public static void checkGroupParam(List<TgDataRangeGroupVO> groupList, Boolean hasCanChoose) {
        RangeTemplateCheckContext context = new RangeTemplateCheckContext();
        context.setCategorySet(new HashSet<>());

        for (int i = 0; i < groupList.size(); i++) {
            TgDataRangeGroupVO tgDataRangeGroupVO = groupList.get(i);

            // 不同树同列 重复校验
            context.setDiffTreeName(new HashSet<>());
            context.setGroupId(i + "");

            // 同组 不同树
            for (TgDataRangeTemplateVO templateVO : tgDataRangeGroupVO.getData()) {
                context.setSameTreeName(new HashSet<>());
                RangeTemplateUtil.checkParam(templateVO, 1, context, hasCanChoose);
            }
        }

        if (context.hasSameCategoryId()) {
            throw new CustomException("自定义列不能重复！", 400);
        }
    }

    /**
     * 1. 自定义名称：不做跨组校验，只做组内， 同树不重复，不同树同列下不重复
     * 2. 自定义列：同组或跨组都不允许重复
     */
    public static void checkParam(TgDataRangeTemplateVO templateVO, int level, RangeTemplateCheckContext context, Boolean hasCanChoose) {
        if (templateVO == null) {
            return;
        }
        Set<String> sameTreeName = context.getSameTreeName();
        if (StringUtils.isEmpty(templateVO.getCategoryName()) || templateVO.getCategoryId() == null) {
            throw new CustomException("名称或者自定义列必填！", 400);
        }

        if (!sameTreeName.add(templateVO.getCategoryName())) {
            throw new CustomException("自定义名称不能重复！", 400);
        }
        if (!context.getDiffTreeName().add(templateVO.getCategoryName() + "#######" + level)) {
            throw new CustomException("自定义名称同列不能重复！", 400);
        }
        if (hasCanChoose && StringUtils.isBlank(templateVO.getGranularity())) {
            throw new CustomException("自定义列设置的关联粒度未选,请补充!", 400);
        }
        context.addCategory(templateVO.getCategoryId(), level);

//        if (!categorySet.add(templateVO.getCategoryId())) {
//            throw new CustomException("自定义列不能重复！", 400);
//        }
        if (!org.springframework.util.CollectionUtils.isEmpty(templateVO.getChildren())) {
            for (TgDataRangeTemplateVO child : templateVO.getChildren()) {
                checkParam(child, level + 1, context, hasCanChoose);
            }
        }
    }

    /**
     * @see RangeTemplateUtil#checkParam(TgDataRangeTemplateVO, Set, Set)
     */
    @Deprecated

    public static void extractCategoryId(List<TgDataRangeTemplateVO> list, Set<Long> categoryIds) {
        if (list == null || list.isEmpty()) {
            return;
        }

        for (TgDataRangeTemplateVO templateVO : list) {
            extractCategoryId(templateVO, categoryIds);
        }
    }


    public static void checkParams(TgDataRangeTemplateVO templateVO, Set<String> nameSet, int i, Map<Long, Integer> categoryIdLevelMap) {
        if (templateVO == null) {
            return;
        }
        if (StringUtils.isEmpty(templateVO.getCategoryName()) || templateVO.getCategoryId() == null) {
            throw new CustomException("名称或者自定义列必填！", 400);
        }

        if (!nameSet.add(templateVO.getCategoryName())) {
            throw new CustomException("名称不能重复！", 400);
        }

        Integer level = categoryIdLevelMap.get(templateVO.getCategoryId());
        if (level == null) {
            categoryIdLevelMap.put(templateVO.getCategoryId(), i);
        } else {
            if (level != i) {
                throw new CustomException("自定义列不能重复！", 400);
            }
        }

        if (!org.springframework.util.CollectionUtils.isEmpty(templateVO.getChildren())) {
            for (TgDataRangeTemplateVO child : templateVO.getChildren()) {
                checkParams(child, nameSet, i + 1, categoryIdLevelMap);
            }
        }
    }

    /**
     * 判断每项必填信息
     *
     * @param dataRangeVO 参数
     * @param errMsg      异常信息
     */
    public static void checkDataRangeNotNull(TgDataRangeVO dataRangeVO, Set<String> errMsg) {
        Boolean hasChoose = !Objects.isNull(dataRangeVO.getHasCanChoose()) && dataRangeVO.getHasCanChoose();
        dataRangeVO.getGroupList().forEach(i -> Optional.ofNullable(i.getData()).orElse(Collections.emptyList()).forEach(e -> checkDataRangeNotNull(e, errMsg, hasChoose)));

    }

    /**
     * 判断每项必填信息
     *
     * @param e      每项元素
     * @param errMsg 异常信息
     */
    public static void checkDataRangeNotNull(TgDataRangeTemplateVO e, Set<String> errMsg, Boolean hasChoose) {
        if (Objects.isNull(e.getCategoryId())) {
            errMsg.add("自定义设置下的自定义列为空,请补充");
        }
        if (hasChoose && StringUtils.isBlank(e.getGranularity())) {
            errMsg.add("自定义列设置的关联粒度未选,请补充");
        }
        if (Objects.nonNull(e.getDataRangeInfo())) {
            for (FilterDTO filter : Optional.ofNullable(e.getDataRangeInfo().getFilters()).orElse(Collections.emptyList())) {
                Filter targetFilter = new Filter();
                ApplicationSqlUtil.convertToFilter(filter, targetFilter);
                ApplicationSqlUtil.FilterContext context = new ApplicationSqlUtil.FilterContext();
                boolean hasEmptyNode = ApplicationSqlUtil.hasEmptyNode(targetFilter, context);
                if (hasEmptyNode || !context.isHasItem()) {
                    errMsg.add("请补全【" + e.getCategoryName() + "】下的数据范围（筛选字段，筛选条件）");
                }
            }
        }
        if (CollectionUtils.isNotEmpty(e.getChildren())) {
            e.getChildren().forEach(i -> checkDataRangeNotNull(i, errMsg, hasChoose));
        }
    }
}
