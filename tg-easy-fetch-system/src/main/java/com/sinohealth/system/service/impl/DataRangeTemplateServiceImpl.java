package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.application.util.ApplicationSqlBuilder;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dict.dao.FieldDictDAO;
import com.sinohealth.system.biz.dict.domain.FieldDict;
import com.sinohealth.system.biz.dict.domain.ProjectCustomFieldDict;
import com.sinohealth.system.biz.dict.mapper.ProjectCustomFieldDictMapper;
import com.sinohealth.system.biz.dict.service.FieldDictService;
import com.sinohealth.system.domain.TgDataRangeTemplate;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.vo.TgDataRangeGroupVO;
import com.sinohealth.system.domain.vo.TgDataRangeTemplateVO;
import com.sinohealth.system.domain.vo.TgDataRangeVO;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.mapper.TgDataRangeTemplateMapper;
import com.sinohealth.system.service.DataRangeTemplateService;
import com.sinohealth.system.util.RangeTemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhangyanping
 * @date 2023/5/15 16:58
 */
@Slf4j
@Service
public class DataRangeTemplateServiceImpl extends ServiceImpl<TgDataRangeTemplateMapper, TgDataRangeTemplate> implements DataRangeTemplateService {

    @Resource
    private TgDataRangeTemplateMapper tgDataRangeTemplateMapper;
    @Resource
    private FieldDictDAO fieldDictDAO;
    @Autowired
    private ProjectCustomFieldDictMapper projectCustomFieldDictMapper;

    @Autowired
    private FieldDictService fieldDictService;

    @Override
    public List<TgDataRangeVO> queryByIds(Collection<Long> templateIds) {
        if (CollectionUtils.isEmpty(templateIds)) {
            return Collections.emptyList();
        }

        List<TgDataRangeTemplate> list = tgDataRangeTemplateMapper.selectBatchIds(templateIds);
        List<TgDataRangeVO> result = new ArrayList<>();
        for (TgDataRangeTemplate template : list) {
            TgDataRangeVO vo = new TgDataRangeVO();
            List<TgDataRangeGroupVO> groupList = JsonUtils.parse(template.getDataRangeConfig(),
                    new TypeReference<List<TgDataRangeGroupVO>>() {
                    });
            vo.setDataRangeId(template.getId());
            vo.setGroupList(groupList);
            result.add(vo);
        }
        return result;
    }

    @Override
    public Set<Long> queryFieldIdsByIds(Collection<Long> templateIds) {
        List<TgDataRangeVO> ranges = this.queryByIds(templateIds);

        return ranges.stream().flatMap(v -> {
            Set<Long> categoryIds = new HashSet<>();
            for (TgDataRangeGroupVO group : v.getGroupList()) {
                TgDataRangeTemplateVO temp = new TgDataRangeTemplateVO();
                temp.setChildren(group.getData());

                RangeTemplateUtil.extractCategoryId(temp, categoryIds);
            }
            return categoryIds.stream();
        }).collect(Collectors.toSet());
    }

    @Override
    public Set<Long> queryFieldIdsByIds(Long projectId, String bizType) {
        ProjectCustomFieldDict latest = projectCustomFieldDictMapper.queryLatestApply(projectId, bizType);
        if (Objects.isNull(latest)) {
            return Collections.emptySet();
        }

        List<ProjectCustomFieldDict> dicts = projectCustomFieldDictMapper.selectList(
                new QueryWrapper<ProjectCustomFieldDict>().lambda()
                        .select(ProjectCustomFieldDict::getFieldDictId)
                        .eq(ProjectCustomFieldDict::getApplicationId, latest.getApplicationId())
                        .eq(ProjectCustomFieldDict::getUpdateTime, latest.getUpdateTime())
                        // 这两个条件只是为了过滤历史id为-1的数据，新数据使用申请id已经能过滤掉
                        .eq(ProjectCustomFieldDict::getProjectId, projectId)
                        .eq(ProjectCustomFieldDict::getBizType, bizType)
        );
        if (CollectionUtils.isNotEmpty(dicts) && dicts.size() == 1) {
            ProjectCustomFieldDict dict = dicts.get(0);
            if (Objects.equals(dict.getFieldDictId(), ApplicationConst.RangeTemplate.EMPTY_USE)) {
                return Collections.emptySet();
            }
        }
        return Lambda.buildSet(dicts, ProjectCustomFieldDict::getFieldDictId);
    }

    @Override
    public Map<Long, String> buildTargetSqlMap(Collection<Long> templateIds, String applicationNo) {
        List<TgDataRangeVO> list = queryByIds(templateIds);
        if (list.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, String> result = new HashMap<>();
        List<String> sqlList;
        for (TgDataRangeVO vo : list) {
            sqlList = new ArrayList<>();
            for (TgDataRangeGroupVO group : vo.getGroupList()) {
                String sql = this.buildSqlBydDataRangeGroup(group);
                if (sql != null) {
                    sqlList.add(sql);
                }
            }
            String caseWhen = String.join("", sqlList);
            caseWhen = caseWhen.substring(0, caseWhen.length() - 1);
            caseWhen = caseWhen.replace("t_1.", "");
            result.put(vo.getDataRangeId(), caseWhen);
        }

        return result;
    }

    @Override
    public String getCreateTablePre(String applicationNo, String caseWhenSql) {
        return "CREATE TABLE temp." + applicationNo + "_zdylab AS SELECT std_id as std_id_zdy," + caseWhenSql + " from edw.cmh_dw_standard_collection  where status not in ('回收站','禁用')";
    }

    /**
     * 字段库名字变更 更新筛选中字段名
     */
    public void fillLatestName(List<TgDataRangeTemplateVO> list) {
        List<FilterDTO> filters = list.stream().map(TgDataRangeTemplateVO::getDataRangeInfo)
                .filter(Objects::nonNull).collect(Collectors.toList());
        fieldDictService.fillFieldNameForFilter(filters);
    }

    public String buildSqlBydDataRangeGroup(TgDataRangeGroupVO groupData) {
        if (groupData == null || groupData.getData() == null) {
            return null;
        }
        //一个分组之间用深度
        List<TgDataRangeTemplateVO> list = groupData.getData();

        //K 第几列 V自定列英文名称
        Map<Integer, String> categoryNameMap = new HashMap<>();
        //K 第几列 V粒度名称， 粒度不一定会设置
        Map<Integer, String> granularityMap = new HashMap<>();
        //K 节点名称 V when后面的condition
        Map<String, String> conditionMap = new HashMap<>();
        //字典表
        Set<Long> categoryIds = new HashSet<>();
        RangeTemplateUtil.extractCategoryId(list, categoryIds);
        Map<Long, FieldDict> dictMap = queryCategoryInfo(categoryIds);
        if (categoryIds.size() != dictMap.size()) {
            throw new CustomException("找不到自定义列，生成自动化SQL异常！");
        }


        //K 深度 V节点名称 按照深度分组的节点
        Map<Integer, List<String>> levelNodeMap = new HashMap<>();
        //工具类
        ApplicationSqlBuilder applicationSqlBuilder = new ApplicationSqlBuilder();

        //递归遍历节点
        this.fillLatestName(list);
        for (TgDataRangeTemplateVO templateVO : list) {
            dfs(templateVO, categoryNameMap, granularityMap, conditionMap, dictMap, 1, applicationSqlBuilder, levelNodeMap);
        }


        //按照列进行聚合，最后的列优先聚合展示
        Integer max = Collections.max(levelNodeMap.keySet());

        StringBuilder builder = new StringBuilder();
        while (max > 0) {
            builder.append("case ");
            List<String> nodes = levelNodeMap.get(max);
            conditionMap.forEach((k, v) -> {
                if (nodes.contains(k)) {
                    builder.append("when " + v + " then '" + k + "' ");
                }
            });


            builder.append("else null end as " + categoryNameMap.get(max) + "_zdy,");
            String granularityName = granularityMap.get(max);
            if (granularityName != null) {
                builder.append("'" + granularityName + "'as " + categoryNameMap.get(max) + "_zdy_class,");
            }

            max--;
        }

        String sql = builder.toString();
        log.info("RangeTemplate SQL：{}", sql);
        return sql;

    }

    void dfs(TgDataRangeTemplateVO templateVO,
             Map<Integer, String> categoryNameMap,
             Map<Integer, String> granularityMap,
             Map<String, String> conditionMap,
             Map<Long, FieldDict> dictMap,
             int level,
             ApplicationSqlBuilder applicationSqlBuilder,
             Map<Integer, List<String>> levelNodeMap

    ) {
        if (templateVO == null) {
            return;
        }

        levelNodeMap.computeIfAbsent(level, v -> new ArrayList<>()).add(templateVO.getCategoryName());

        if (templateVO.getCategoryId() != null) {
            categoryNameMap.put(level, dictMap.get(templateVO.getCategoryId()).getFieldName());
        }

        if (StringUtils.isNotEmpty(templateVO.getGranularity())) {
            granularityMap.put(level, templateVO.getGranularity());
        }

        if (templateVO.getDataRangeInfo() != null) {
            String condition = applicationSqlBuilder.buildRangeByMySQL(templateVO.getDataRangeInfo(), 1);
            //名称不允许重复且不为空，因此可以做唯一键
            String uniqueKey = templateVO.getCategoryName();
            conditionMap.put(uniqueKey, "(" + condition + ")");
        }


        if (CollectionUtils.isNotEmpty(templateVO.getChildren())) {
            List<String> conditions = new ArrayList<>(templateVO.getChildren().size());
            for (TgDataRangeTemplateVO child : templateVO.getChildren()) {
                dfs(child, categoryNameMap, granularityMap, conditionMap, dictMap, level + 1, applicationSqlBuilder, levelNodeMap);
                conditions.add(conditionMap.get(child.getCategoryName()));
            }

            String sql = String.join(" or ", conditions);
            conditionMap.put(templateVO.getCategoryName(), conditions.size() > 1 ? "(" + sql + ")" : sql);
        }

    }


    /**
     * CK数据库的规则
     */
    @Override
    public String buildCreateTableSql(String applicationNo, TgDataRangeTemplateVO vo, Map<Long, FieldDict> dictMap) {
        //临时节点做通用逻辑处理
        Map<TgDataRangeTemplateVO, List<String>> conditionMap = new LinkedHashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        dfs(vo, 0, conditionMap, new ApplicationSqlBuilder(), stringBuilder, dictMap);
        stringBuilder.deleteCharAt(stringBuilder.length() - 2);

        log.info("conditionMap:{}", conditionMap);

        String sql = stringBuilder.toString();
        sql = sql.replace("t_1.", "");
        log.info("生成SQL>>>>>:{}", sql);
        return sql;
    }

    private Map<Long, FieldDict> queryCategoryInfo(Set<Long> categoryIds) {
        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return fieldDictDAO.listByIds(categoryIds).stream().collect(Collectors.toMap(FieldDict::getId, x -> x));
    }


    /**
     * 深度递归
     */
    void dfs(TgDataRangeTemplateVO root, int i,
             Map<TgDataRangeTemplateVO, List<String>> conditionMap,
             ApplicationSqlBuilder applicationSqlBuilder,
             StringBuilder stringBuilder,
             Map<Long, FieldDict> dictMap) {
        if (root == null) {
            return;
        }

        //凑这一层级的
        if (root.getDataRangeInfo() != null) {
            String condition = applicationSqlBuilder.buildRangeByMySQL(root.getDataRangeInfo(), 1);
            conditionMap.computeIfAbsent(root, v -> new ArrayList<>()).add(condition);
        }

        if (CollectionUtils.isNotEmpty(root.getChildren())) {

            boolean useCase = true;
            for (int x = 0; x < root.getChildren().size(); x++) {
                //设置自定义列信息
                TgDataRangeTemplateVO child = root.getChildren().get(x);
                FieldDict fieldDict = dictMap.get(child.getCategoryId());
                child.setCategoryChineseName(fieldDict == null ? "key" + child.getCategoryId() : fieldDict.getName());
                child.setCategoryEnName(fieldDict == null ? "key" + child.getCategoryId() : fieldDict.getFieldName());

                //深度递归
                dfs(child, i + 1, conditionMap, applicationSqlBuilder, stringBuilder, dictMap);

                List<String> childCondition = conditionMap.computeIfAbsent(child, v -> new ArrayList<>());
                conditionMap.computeIfAbsent(root, v -> new ArrayList<>()).addAll(childCondition);

                //聚合SQL第一个
                if (useCase) {
                    stringBuilder.append("case ");
                    useCase = false;
                }
                stringBuilder.append("when (");

                for (int j = 0; j < childCondition.size(); j++) {
                    stringBuilder.append("(").append(childCondition.get(j)).append(")");
                    if (j < childCondition.size() - 1) {
                        stringBuilder.append(" or ");
                    }
                }
                stringBuilder.append(") then '").append(child.getCategoryName()).append("' ");
            }

            //获取当前层级的标题头 ，自定义列中英文名称，粒度名称
            TgDataRangeTemplateVO title = root.getChildren().get(0);
            //拼接别名
            stringBuilder.append("else null end as ").append(title.getCategoryEnName()).append("_zdy");
            if (root.getChildren().get(0).getGranularity() != null) {
                stringBuilder.append(", '")
                        .append(title.getGranularity())
                        .append("' as ")
                        .append(title.getCategoryEnName())
                        .append("_zdy_class");
            }
            stringBuilder.append(", ");
        }
    }
}
