package com.sinohealth.system.util;

import cn.hutool.core.util.ReUtil;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLDataTypeImpl;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLPrimaryKeyImpl;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectStatement;
import com.alibaba.druid.sql.dialect.postgresql.parser.PGSQLStatementParser;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSONArray;
import com.sinohealth.bi.constant.FunctionalOperatorType;
import com.sinohealth.bi.constant.SqlConstant;
import com.sinohealth.bi.data.ClickHouse;
import com.sinohealth.bi.data.Filter;
import com.sinohealth.bi.data.Table;
import com.sinohealth.bi.enums.FunctionalOperatorEnum;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.enums.SortingFieldEnum;
import com.sinohealth.common.enums.dict.DivisorModeEnum;
import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.StrUtil;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.application.dto.ApplicationGranularityDto;
import com.sinohealth.system.biz.application.dto.request.ApplicationSaveAsRequest;
import com.sinohealth.system.biz.ck.constant.CkClusterType;
import com.sinohealth.system.biz.ck.constant.CkTableSuffixTable;
import com.sinohealth.system.biz.dataassets.service.impl.UserDataAssetsServiceImpl;
import com.sinohealth.system.biz.dict.util.Expression;
import com.sinohealth.system.biz.dict.util.ExpressionWord;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.vo.TgDataRangeTemplateVO;
import com.sinohealth.system.dto.ApplicationDataDto;
import com.sinohealth.system.dto.TableDataDto;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.dto.application.JoinInfoDto;
import com.sinohealth.system.service.impl.DataManageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-12-01 17:51
 */
@Slf4j
public class ApplicationSqlUtil {

    public static final String query_prefix = "SELECT * FROM a WHERE ";

    public static final String NULL_FLAG = "(空值)";
    public static final String NULL_FLAG_VAL = "''";
    public static final String ASSETS_TABLE_PREFIX = "tg_assets_";
    public static final String WIDE_ASSETS_TABLE_PREFIX = "tg_assets_wd_";

    /**
     * 多行SQL的分隔符
     */
    public static final String ROW_SPLIT = ";;";

    /**
     * 导出Excel时追加的列名
     */
    public static final String PROJECT_NAME_VAL = "项目名称";
    /**
     * 用于隔离模板和申请 出现同名别名的情况 预设指标才会使用到
     */
    public static final String CUSTOM_METRIC_SUFFIX = "　";
    /**
     * 预览数据时 字段库有排序的字段 -> 关联表字段 -> 指标字段 -> 未设置排序的字段
     */
    public static final int nullSort = 99999999;
    public static final int metricsSort = 11_0000;
    public static final int joinSort = 10_0000;

    private static final Map<SQLBinaryOperator, SQLBinaryOperator> reverseMap = new HashMap<>();

    static void initReverseMap(SQLBinaryOperator a, SQLBinaryOperator b) {
        reverseMap.put(a, b);
        reverseMap.put(b, a);
    }

    static {
        initReverseMap(SQLBinaryOperator.Equality, SQLBinaryOperator.NotEqual);
        initReverseMap(SQLBinaryOperator.Like, SQLBinaryOperator.NotLike);
        initReverseMap(SQLBinaryOperator.POSIX_Regular_Match, SQLBinaryOperator.POSIX_Regular_Not_Match);
        initReverseMap(SQLBinaryOperator.Is, SQLBinaryOperator.IsNot);
        initReverseMap(SQLBinaryOperator.LessThanOrEqual, SQLBinaryOperator.GreaterThan);
        initReverseMap(SQLBinaryOperator.GreaterThanOrEqual, SQLBinaryOperator.LessThan);
    }

    public static class FilterContext {
        private boolean hasItem;

        public FilterContext() {
        }

        public FilterContext(boolean hasItem) {
            this.hasItem = hasItem;
        }

        public boolean isHasItem() {
            return hasItem;
        }

        public void setHasItem(boolean hasItem) {
            this.hasItem = hasItem;
        }
    }

    public static String buildWideTableName(Long id) {
        String now = DateUtils.dateTimeNow();
        return ApplicationSqlUtil.WIDE_ASSETS_TABLE_PREFIX + StrUtil.randomAlpha(4) + "_" + id + "_" + now + "_shard";
    }

    public static String buildSnapshotWideTableName(Long id) {
        String now = DateUtils.dateTimeNow();
        return ApplicationSqlUtil.WIDE_ASSETS_TABLE_PREFIX + StrUtil.randomAlpha(4) + "_" + id + "_" + now + "_snap";
    }

    public static String buildTableName(Long id) {
        String now = DateUtils.dateTimeNow();
        return ApplicationSqlUtil.WIDE_ASSETS_TABLE_PREFIX + StrUtil.randomAlpha(4)
                + "_" + id + "_" + now + CkTableSuffixTable.SNAP;
    }

    public static String trimMetricSuffix(String name) {
        if (name.endsWith(ApplicationSqlUtil.CUSTOM_METRIC_SUFFIX)) {
            return name.substring(0, name.length() - ApplicationSqlUtil.CUSTOM_METRIC_SUFFIX.length());
        }
        return name;
    }

    public static boolean checkRepeatJoin(List<JoinInfoDto> joinInfo) {
        if (CollectionUtils.isEmpty(joinInfo)) {
            return false;
        }
        Set<Long> left = joinInfo.stream().map(JoinInfoDto::getTableId1)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> right = joinInfo.stream().map(JoinInfoDto::getTableId2)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return left.stream().anyMatch(right::contains);
    }

    public static boolean hasEmptyNode(TgDataRangeTemplateVO vo) {
        boolean isLeaf = CollectionUtils.isEmpty(vo.getChildren());
        if (!isLeaf) {
            for (TgDataRangeTemplateVO child : vo.getChildren()) {
                boolean hasEmptyNode = hasEmptyNode(child);
                if (hasEmptyNode) {
                    return true;
                }
            }
            return false;
        }

        FilterDTO filter = vo.getDataRangeInfo();
        if (Objects.isNull(filter)) {
            return true;
        }
        Filter targetFilter = new Filter();
        ApplicationSqlUtil.convertToFilter(filter, targetFilter);
        ApplicationSqlUtil.FilterContext context = new ApplicationSqlUtil.FilterContext();
        boolean hasEmptyNode = ApplicationSqlUtil.hasEmptyNode(targetFilter, context);
        return hasEmptyNode || !context.isHasItem();
    }

    /**
     * 校验 字段，操作符，值 存在空值，或者全部节点都是空
     */
    public static boolean hasEmptyNode(Filter filter, FilterContext context) {
        List<Filter> filters = filter.getFilters();

        if (CollectionUtils.isNotEmpty(filters)) {
            for (Filter son : filters) {
                boolean hasEmpty = hasEmptyNode(son, context);
                if (hasEmpty) {
                    return true;
                }
            }
        }
        Filter.FilterItemDTO item = filter.getFilterItem();
        if (Objects.nonNull(item)) {
            String msg = String.format("%s/%s [%s] %s", item.getFieldId(), item.getFieldName(), item.getFunctionalOperator(), item.getValue());
            if (Objects.isNull(item.getFieldId()) && StringUtils.isBlank(item.getFieldName())) {
                log.warn("msg={}", msg);
                return true;
            }
            final FunctionalOperatorEnum opt = FunctionalOperatorEnum
                    .getFunctionalOperatorEnum(item.getFunctionalOperator());
            if (Objects.isNull(opt)) {
                log.warn("msg={}", msg);
                return true;
            }

            boolean notNeedVal = Objects.equals(opt, FunctionalOperatorEnum.EMPTY) || Objects.equals(opt, FunctionalOperatorEnum.NOT_EMPTY);
            if (!notNeedVal && StringUtils.isBlank(item.getValue())) {
                log.warn("msg={}", msg);
                return true;
            }

            context.setHasItem(true);
        }
        return false;
    }


    public static void extractFieldId(FilterDTO filter, List<Long> fields) {
        if (Objects.isNull(filter)) {
            return;
        }
        if (CollectionUtils.isNotEmpty(filter.getFilters())) {
            for (FilterDTO next : filter.getFilters()) {
                extractFieldId(next, fields);
            }
        }
        FilterDTO.FilterItemDTO item = filter.getFilterItem();
        if (Objects.isNull(item)) {
            return;
        }
        if (CollectionUtils.isNotEmpty(item.getFilters())) {
            for (FilterDTO next : item.getFilters()) {
                extractFieldId(next, fields);
            }
        }
        if (Objects.nonNull(item.getFieldId())) {
            fields.add(item.getFieldId());
        }
    }

    public static void extractFieldId(FilterDTO filter, List<Long> table, List<String> fields) {
        if (Objects.isNull(filter)) {
            return;
        }
        if (CollectionUtils.isNotEmpty(filter.getFilters())) {
            for (FilterDTO next : filter.getFilters()) {
                extractFieldId(next, table, fields);
            }
        }
        FilterDTO.FilterItemDTO item = filter.getFilterItem();
        if (Objects.isNull(item)) {
            return;
        }
        if (CollectionUtils.isNotEmpty(item.getFilters())) {
            for (FilterDTO next : item.getFilters()) {
                extractFieldId(next, table, fields);
            }
        }
        if (StringUtils.isNotBlank(item.getFieldName())) {
            fields.add(item.getFieldName());
        }
        if (Objects.nonNull(item.getTableId())) {
            table.add(item.getTableId());
        }
    }

    /**
     * 定制化处理： 支持 IN 查询
     */
    public static void convertToFilter(FilterDTO filter, Filter result) {
        List<FilterDTO> filters = filter.getFilters();
        if (CollectionUtils.isNotEmpty(filters)) {
            ArrayList<Filter> childes = new ArrayList<>();
            result.setFilters(childes);
            for (FilterDTO filterDTO : filters) {
                // 展开 item -> filters -> filters, 忽略前端组件的father节点
                if (Objects.equals(filterDTO.getIsFather(), 1)) {
                    handleFatherNode(filterDTO, childes);
                } else {
                    Filter child = new Filter();
                    childes.add(child);
                    convertToFilter(filterDTO, child);
                }
            }
        } else {
            ArrayList<Filter> childes = new ArrayList<>();
            result.setFilters(childes);
            handleFatherNode(filter, childes);
        }
        // 当 当前节点是中间节点时，已经被 handleFatherNode 处理，需要屏蔽 filterItem 的值
        if (Objects.equals(filter.getIsFather(), 1)) {
            return;
        }

        result.setLogicalOperator(filter.getLogicalOperator());

        FilterDTO.FilterItemDTO item = filter.getFilterItem();
        if (Objects.isNull(item)) {
            return;
        }


        // TODO 字段是否为空的情况
//        if (Objects.isNull(item.getFieldId())) {
//            return;
//        }

        if (Objects.isNull(item.getValue())) {
            result.setFilterItem(JsonUtils.parse(JsonUtils.format(item), Filter.FilterItemDTO.class));
            return;
        }

        // 数组
        if (item.getValue() instanceof ArrayList || item.getValue() instanceof JSONArray) {
            Filter.FilterItemDTO targetItem = new Filter.FilterItemDTO();
            BeanUtils.copyProperties(item, targetItem);
            if (item.getValue() instanceof ArrayList) {
                ArrayList values = (ArrayList) item.getValue();
                if (org.apache.commons.collections4.CollectionUtils.size(values) == 2) {
                    targetItem.setValue(values.get(0).toString());
                    targetItem.setOtherValue(values.get(1).toString());
                    result.setFilterItem(targetItem);
                }
            } else {
                JSONArray values = (JSONArray) item.getValue();
                if (org.apache.commons.collections4.CollectionUtils.size(values) == 2) {
                    targetItem.setValue(values.get(0).toString());
                    targetItem.setOtherValue(values.get(1).toString());
                    result.setFilterItem(targetItem);
                }
            }
        } else if (FunctionalOperatorType.EQUAL_TYPES.contains(item.getFunctionalOperator())
                && item.getValue().toString().contains(SqlConstant.IN_OR)) {
            // 字符串且
            String valuePair = Stream.of(item.getValue().toString().split(SqlConstant.IN_OR_REG))
                    .map(v -> {
                        if (Objects.equals(v, NULL_FLAG)) {
                            return NULL_FLAG_VAL;
                        } else {
                            return String.format("'%s'", v);
                        }
                    }).collect(Collectors.joining(","));

            Filter.FilterItemDTO targetItem = JsonUtils.parse(JsonUtils.format(item), Filter.FilterItemDTO.class);
            if (Objects.nonNull(targetItem) && StringUtils.isNoneBlank(valuePair)) {
                targetItem.setValue("(" + valuePair + ")");
                if (Objects.equals(FunctionalOperatorEnum.EQUAL_TO.getType(), item.getFunctionalOperator())) {
                    targetItem.setFunctionalOperator(FunctionalOperatorEnum.IN.getType());
                } else {
                    targetItem.setFunctionalOperator(FunctionalOperatorEnum.NOT_IN.getType());
                }
                result.setFilterItem(targetItem);
            }
        } else if (FunctionalOperatorType.CONTAIN_TYPES.contains(item.getFunctionalOperator())) {
            Filter.FilterItemDTO targetItem = JsonUtils.parse(JsonUtils.format(item), Filter.FilterItemDTO.class);
            if (Objects.nonNull(targetItem)) {
                targetItem.setContainExpend(true);
                result.setFilterItem(targetItem);
            }
        } else if (FunctionalOperatorType.EQUAL_TYPES.contains(item.getFunctionalOperator())) {
            Object value = item.getValue();
            if (Objects.equals(value, NULL_FLAG)) {
                item.setValue(NULL_FLAG_VAL);
            }
            result.setFilterItem(JsonUtils.parse(JsonUtils.format(item), Filter.FilterItemDTO.class));
        } else {
            result.setFilterItem(JsonUtils.parse(JsonUtils.format(item), Filter.FilterItemDTO.class));
        }
    }

    /**
     * 移除前端组件内 非条件数据的节点
     */
    private static void handleFatherNode(FilterDTO filter, ArrayList<Filter> childes) {
        if (!Objects.equals(filter.getIsFather(), 1)) {
            return;
        }

        Filter child = new Filter();
        childes.add(child);

        ArrayList<Filter> nestedChildes = new ArrayList<>();
        child.setFilters(nestedChildes);
        child.setLogicalOperator(Optional.ofNullable(filter.getFilterItem())
                .map(FilterDTO.FilterItemDTO::getAndOr).orElseGet(() -> {
                    log.warn("empty logic operator {}", filter.getFilterItem());
                    return "and";
                }));

        Optional.ofNullable(filter.getFilterItem())
                .map(FilterDTO.FilterItemDTO::getFilters)
                .filter(CollectionUtils::isNotEmpty)
                .map(v -> v.get(0))
                .map(FilterDTO::getFilters)
                .filter(CollectionUtils::isNotEmpty)
                .ifPresent(v -> {
                    // 为前端错误数据强行修正
                    Optional.ofNullable(v.get(0))
                            .map(FilterDTO::getFilterItem)
                            .map(FilterDTO.FilterItemDTO::getAndOr)
                            .ifPresent(child::setLogicalOperator);
                    for (FilterDTO nestedFilter : v) {
                        Filter nestedChild = new Filter();
                        nestedChildes.add(nestedChild);
                        nestedChild.setLogicalOperator(nestedFilter.getLogicalOperator());
                        convertToFilter(nestedFilter, nestedChild);
                    }
                });
    }

    public static void fillFilterV2(FilterDTO filter, Filter result) {
        List<FilterDTO> filters = filter.getFilters();
        if (CollectionUtils.isNotEmpty(filters)) {
            ArrayList<Filter> childes = new ArrayList<>();
            result.setFilters(childes);
            for (FilterDTO filterDTO : filters) {
                // 展开 item -> filters -> filters, 忽略前端组件的father节点
                if (Objects.equals(filterDTO.getIsFather(), 1)) {
                    Filter child = new Filter();
                    childes.add(child);

                    ArrayList<Filter> nestedChildes = new ArrayList<>();
                    child.setFilters(nestedChildes);
                    child.setLogicalOperator(Optional.ofNullable(filterDTO.getFilterItem())
                            .map(FilterDTO.FilterItemDTO::getAndOr).orElseGet(() -> {
                                log.warn("empty logic operator {}", filterDTO.getFilterItem());
                                return "and";
                            }));

                    Optional.ofNullable(filterDTO.getFilterItem()).map(FilterDTO.FilterItemDTO::getFilters)
                            .filter(org.apache.commons.collections4.CollectionUtils::isNotEmpty)
                            .ifPresent(v -> v.forEach(i -> {
                                if (org.apache.commons.collections4.CollectionUtils.isEmpty(i.getFilters())) {
                                    return;
                                }
                                for (FilterDTO nestedFilter : i.getFilters()) {
                                    Filter nestedChild = new Filter();
                                    nestedChildes.add(nestedChild);
                                    nestedChild.setLogicalOperator(nestedFilter.getLogicalOperator());
                                    fillFilterV2(nestedFilter, nestedChild);
                                }
                            }));

//                    Optional.ofNullable(filterDTO.getFilterItem()).map(FilterDTO.FilterItemDTO::getFilters)
//                            .filter(org.apache.commons.collections4.CollectionUtils::isNotEmpty)
//                            .map(v -> v.get(0))
//                            .map(FilterDTO::getFilters).ifPresent(v -> {
//                                for (FilterDTO nestedFilter : v) {
//                                    Filter nestedChild = new Filter();
//                                    nestedChildes.add(nestedChild);
//                                    nestedChild.setLogicalOperator(nestedFilter.getLogicalOperator());
//                                    this.fillFilter(nestedFilter, nestedChild);
//                                }
//                            });
                } else {
                    Filter child = new Filter();
                    childes.add(child);
                    fillFilterV2(filterDTO, child);
                }
            }
        }

        result.setLogicalOperator(filter.getLogicalOperator());

        FilterDTO.FilterItemDTO item = filter.getFilterItem();
        if (Objects.isNull(item)) {
            return;
        }

        if (Objects.isNull(item.getValue())) {
            result.setFilterItem(JsonUtils.parse(JsonUtils.format(item), Filter.FilterItemDTO.class));
            return;
        }

        // 数组
        if (item.getValue() instanceof ArrayList) {
            Filter.FilterItemDTO targetItem = new Filter.FilterItemDTO();
            BeanUtils.copyProperties(item, targetItem);
            ArrayList values = (ArrayList) item.getValue();
            if (org.apache.commons.collections4.CollectionUtils.size(values) == 2) {
                targetItem.setValue(values.get(0).toString());
                targetItem.setOtherValue(values.get(1).toString());
                result.setFilterItem(targetItem);
            }
        } else if (FunctionalOperatorType.EQUAL_TYPES.contains(item.getFunctionalOperator())
                && item.getValue().toString().contains(SqlConstant.IN_OR)) {
            // 字符串且
            String valuePair = Stream.of(item.getValue().toString().split(SqlConstant.IN_OR_REG))
                    .map(v -> {
                        if (Objects.equals(v, NULL_FLAG)) {
                            return "''";
                        } else {
                            return String.format("'%s'", v);
                        }
                    }).collect(Collectors.joining(","));

            Filter.FilterItemDTO targetItem = JsonUtils.parse(JsonUtils.format(item), Filter.FilterItemDTO.class);
            if (Objects.nonNull(targetItem) && StringUtils.isNoneBlank(valuePair)) {
                targetItem.setValue("(" + valuePair + ")");
                if (Objects.equals(FunctionalOperatorEnum.EQUAL_TO.getType(), item.getFunctionalOperator())) {
                    targetItem.setFunctionalOperator(FunctionalOperatorEnum.IN.getType());
                } else {
                    targetItem.setFunctionalOperator(FunctionalOperatorEnum.NOT_IN.getType());
                }
                result.setFilterItem(targetItem);
            }
        } else if (FunctionalOperatorType.CONTAIN_TYPES.contains(item.getFunctionalOperator())) {
            Filter.FilterItemDTO targetItem = JsonUtils.parse(JsonUtils.format(item), Filter.FilterItemDTO.class);
            if (Objects.nonNull(targetItem)) {
                targetItem.setContainExpend(true);
                result.setFilterItem(targetItem);
            }
        } else {
            result.setFilterItem(JsonUtils.parse(JsonUtils.format(item), Filter.FilterItemDTO.class));
        }
    }

    public static List<String> getSortedFieldList(String sql) {
        int from = sql.indexOf("FROM");
        String fields = sql.substring(0, from);
        String periodStr = " as period_type";
        int periodIdx = fields.indexOf(periodStr);
        List<String> result = new ArrayList<>();
        if (periodIdx > 0) {
            fields = fields.substring(periodIdx + periodStr.length());
            result.add(ApplicationConst.PeriodField.PERIOD_STR);
            result.add(ApplicationConst.PeriodField.PERIOD_NEW);
            result.add(ApplicationConst.PeriodField.PERIOD_TYPE);
        }

        fields = StringUtils.replace(fields, "SELECT", "");

        Arrays.stream(fields.split(",")).filter(StringUtils::isNotBlank).map(v -> {
            String[] pair = v.split(" ");
            return pair[pair.length - 1].trim();
        }).forEach(result::add);
        return result;
    }

    public static void fillLengthAndType(TableFieldInfo field) {
        int index = field.getDataType().lastIndexOf("(");
        if (index > 0) {
            Pattern decimalTwo = Pattern.compile("(Decimal.*)\\((\\d+),\\s?(\\d+)\\)");
            Pattern decimalOne = Pattern.compile("(Decimal.*)\\((\\d+)\\)");

            String originDataType = RegExUtils.replaceFirst(field.getDataType(), "^Nullable\\((.*)\\)", "$1");
            List<String> list = ReUtil.getAllGroups(decimalTwo, originDataType, false);
            if (CollectionUtils.isNotEmpty(list)) {
                field.setDataType(list.get(0));
                field.setLength(Integer.parseInt(list.get(1)));
                field.setScale(Integer.parseInt(list.get(2)));
                return;
            }

            list = ReUtil.getAllGroups(decimalOne, originDataType, false);
            if (CollectionUtils.isNotEmpty(list)) {
                field.setDataType(list.get(0));
                field.setLength(Integer.parseInt(list.get(1)));
                return;
            }
            field.setDataType(originDataType);
        }
    }

    public static String trimLengthAndType(String dataType) {
        int index = dataType.lastIndexOf("(");
        if (index > 0) {
            Pattern decimalTwo = Pattern.compile("(Decimal.*)\\((\\d+),\\s?(\\d+)\\)");
            Pattern decimalOne = Pattern.compile("(Decimal.*)\\((\\d+)\\)");

            String originDataType = RegExUtils.replaceFirst(dataType, "^Nullable\\((.*)\\)", "$1");
            List<String> list = ReUtil.getAllGroups(decimalTwo, originDataType, false);
            if (CollectionUtils.isNotEmpty(list)) {
                return list.get(0);
            }

            list = ReUtil.getAllGroups(decimalOne, originDataType, false);
            if (CollectionUtils.isNotEmpty(list)) {
                return list.get(0);
            }
            return originDataType;
        }
        return dataType;
    }

    /**
     * @return shard表
     */
    public static String buildAssetsTableName(Long applicationId) {
        String now = DateUtils.dateTimeNow();
        return ASSETS_TABLE_PREFIX + applicationId + "_" + now + "_shard";
    }

    public static String appendDb(String querySQL, String db) {
        String result = ReUtil.replaceAll(querySQL, "FROM " + ApplicationSqlUtil.ASSETS_TABLE_PREFIX + "(\\w+)",
                "FROM " + db + "." + ApplicationSqlUtil.ASSETS_TABLE_PREFIX + "$1");
        return ReUtil.replaceAll(result, "FROM (\\w+) t_1", "FROM " + db + ".$1 t_1");
    }

    /**
     * 处理精度，除数0 构建出 预设指标 表达式
     */
    public static Optional<String> buildFormula(String formula, Map<String, String> nameMap,
                                                Integer round, String divisorMode) {
        Expression expression = new Expression(formula);
        List<ExpressionWord> words = expression.getWord();

        if (words.size() != 3) {
            return Optional.empty();
        }
        String result;
        String sec = nameMap.get(words.get(2).getWord());
        // 操作符
        String operator = words.get(1).getWord();
        if (Objects.equals("/", operator)) {
            if (Objects.equals(DivisorModeEnum.zero_result.name(), divisorMode)) {
                result = String.format("IF(%s = 0 OR %s IS NULL, 0, %s / materialize(%s))",
                        sec, sec, nameMap.get(words.get(0).getWord()), sec);
            } else {
                result = String.format("IF(%s = 0 OR %s IS NULL, NULL, %s / materialize(%s))",
                        sec, sec, nameMap.get(words.get(0).getWord()), sec);
            }
        } else {
            result = String.format("%s%s%s", nameMap.get(words.get(0).getWord()), operator, sec);
        }

        String flag = MDC.get(CommonConstants.REMOVE_ROUND);
        if (Objects.nonNull(round) && Objects.isNull(flag)) {
            result = String.format("ROUND(cast(%s as Nullable(Decimal(30,11))),%d)", result, round);
        }
        return Optional.of(result);
    }


    /**
     * 校验SQL是否合法
     */
    public static Pair<String, String> checkSql(String sql) {
        if (StringUtils.isBlank(sql)) {
            throw new CustomException("sql为空");
        }

        if (!sql.startsWith("SELECT") && !sql.startsWith("select")) {
            throw new CustomException("仅支持select语句");
        }
        try {
            List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, JdbcConstants.CLICKHOUSE);
            SQLSelectStatement sqlStatement = (SQLSelectStatement) sqlStatements.get(0);

            SQLSelectQueryBlock block = (SQLSelectQueryBlock) sqlStatement.getSelect().getQuery();

            List<SQLSelectItem> selectList = block.getSelectList();
            String[] originCols = selectList.stream().map(v -> v.getExpr().toString()).toArray(String[]::new);
            if (originCols.length > 1) {
                throw new CustomException("仅支持查询单个字段值");
            }
            String firstCol = originCols[0];
            if (Objects.equals(firstCol, "*")) {
                throw new CustomException("仅支持查询单个字段值，不支持*");
            }
            if (firstCol.contains("(")) {
                throw new CustomException("仅支持查询原始字段值，不支持函数");
            }
            SQLExprTableSource source = (SQLExprTableSource) block.getFrom();
            String table = ((SQLIdentifierExpr) source.getExpr()).getName();
            log.info("sql={} firstCol={} table={}", sql, firstCol, table);
            return Pair.create(firstCol, table);
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            log.error("", e);
            throw new CustomException("仅支持select语句");
        }
    }

    /**
     * @see UserDataAssetsServiceImpl#saveAs(ApplicationSaveAsRequest) 强依赖 另存SQL拼接的格式
     */
    public static Optional<String> replaceRelationTable(String sql, String old, String newTable) {
        if (!StringUtils.contains(sql, old)) {
            return Optional.empty();
        }
        return Optional.of(sql.replace(old, newTable));
    }

    public static Optional<String> replaceRelationTable(String sql, String newTable) {
        String result = ReUtil.replaceAll(sql, "\\( SELECT \\* FROM \\w+ \\) tt",
                "( SELECT * FROM " + newTable + " ) tt");
        if (result.contains(newTable)) {
            return Optional.of(result);
        }
        return Optional.empty();
    }


    /**
     * 将临时表SQL处理为目标建表语句
     *
     * @see CommonConstants#ROUND_BLOCK 构建指标查询SQL时同样做类型强转
     */
    public static String convertCkSqlByDruid(String createSQL,
                                             Map<String, ApplicationDataDto.Header> headerMap,
                                             String cluster, String db) {
        int engineIdx = createSQL.indexOf("ENGINE");
        String table = createSQL.substring(0, engineIdx);
        String suffix = createSQL.substring(engineIdx);

        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(table, JdbcConstants.CLICKHOUSE);
        if (CollectionUtils.isEmpty(sqlStatements)) {
            throw new RuntimeException("SQL解析错误");
        }
        SQLCreateTableStatement statement = (SQLCreateTableStatement) sqlStatements.get(0);
        List<SQLTableElement> fields = statement.getTableElementList();
        if (Objects.isNull(fields)) {
            throw new RuntimeException("SELECT查询字段为空");
        }

        String clusterStr = Optional.ofNullable(cluster).map(v -> " ON cluster " + v).orElse("");

        StringBuilder res = new StringBuilder();
        for (SQLTableElement field : fields) {
            SQLColumnDefinition def = (SQLColumnDefinition) field;
            String columnName = def.getName().getSimpleName();
            String originName = columnName.replace("`", "");

            ApplicationDataDto.Header alias = headerMap.get(originName);
            res.append(def.getName()).append(" ");
            String dataType = def.getDataType().toString();
            // 强制处理资产表的类型
            if (StringUtils.containsIgnoreCase(dataType, "Float64")) {
                if (StringUtils.containsIgnoreCase(dataType, "Nullable")) {
                    String finalType = dataType.replace("Nullable(Float64)", "Nullable(Decimal(30, 11))");
                    res.append(finalType);
                } else {
                    String finalType = dataType.replace("Float64", "Nullable(Decimal(30, 11))");
                    res.append(finalType);
                }
            } else if (StringUtils.isBlank(clusterStr) && StringUtils.containsIgnoreCase(dataType, "Decimal")) {
                res.append(shortDecimalType(def.getDataType()));
            } else {
                res.append(dataType);
            }

            String finalAlias = Optional.ofNullable(alias).map(ApplicationDataDto.Header::getFiledAlias).orElse("");
            res.append(" COMMENT '").append(finalAlias).append("',");
        }
        String tableNameF = statement.getTableSource().getName().getSimpleName().replace(db + ".", "");
        String field = res.substring(0, res.length() - 1);

        return " CREATE TABLE IF NOT EXISTS " + tableNameF + clusterStr + " (" + field + ") " + suffix;
    }

    /**
     * 由于HDFS orc表不支持超过Decimal(38,11)精度的字段，业务也没实际意义，因此降低字段精度
     */
    static String shortDecimalType(SQLDataType type) {
        if (type instanceof SQLDataTypeImpl) {
            SQLDataTypeImpl impl = (SQLDataTypeImpl) type;
            if (Objects.equals(impl.getName(), "Nullable")) {
                SQLMethodInvokeExpr sqlExpr = (SQLMethodInvokeExpr) impl.getArguments().get(0);
                modifArgs(sqlExpr.getArguments());
                return type.toString();
            } else {
                modifArgs(impl.getArguments());
                return "Nullable(" + type + ")";
            }
        }

        throw new CustomException("不支持的类型 " + type.toString());
    }

    private static void modifArgs(List<SQLExpr> args) {
        SQLIntegerExpr val = (SQLIntegerExpr) args.get(0);
        if (val.getNumber().intValue() > 30) {
            val.setNumber(30);
        }
        val = (SQLIntegerExpr) args.get(1);
        if (val.getNumber().intValue() > 11) {
            val.setNumber(11);
        }
    }

    public static Map<String, Integer> parseCKColumnSort(String createSQL) {
        int engineIdx = createSQL.indexOf("ENGINE");
        if (engineIdx == -1) {
            engineIdx = createSQL.indexOf("engine");
        }
        String table = createSQL.substring(0, engineIdx);

        table = table.replace("on cluster " + CkClusterType.DEFAULT, "");

        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(table, JdbcConstants.CLICKHOUSE);
        if (CollectionUtils.isEmpty(sqlStatements)) {
            throw new RuntimeException("SQL解析错误");
        }
        SQLCreateTableStatement statement = (SQLCreateTableStatement) sqlStatements.get(0);
        List<SQLTableElement> fields = statement.getTableElementList();
        if (Objects.isNull(fields)) {
            throw new RuntimeException("SELECT查询字段为空");
        }
        Map<String, Integer> result = new HashMap<>();

        for (int i = 0; i < fields.size(); i++) {
            SQLTableElement field = fields.get(i);
            String originName;
            if (field instanceof SQLPrimaryKeyImpl) {
                continue;
            }

            SQLColumnDefinition def = (SQLColumnDefinition) field;
            String columnName = def.getName().getSimpleName();
            originName = columnName.replace("`", "");
            result.put(originName, i + 1);
        }
        return result;
    }

    public static String getLocalTableName(String shardTableName) {
        return shardTableName.replace(CkTableSuffixTable.SHARD, CkTableSuffixTable.LOCAL);
    }

    public static String getSnapshotTableName(String shardTableName) {
        return shardTableName.replace(CkTableSuffixTable.SHARD, CkTableSuffixTable.SNAP);
    }


    /**
     * @see DataManageServiceImpl#buildSelectSql(String, List, String, Integer, Integer, List, String, String, List)
     */
    public static String buildSelectSql(String tableName, List<TableFieldInfo> fields, String whereSql,
                                        Integer startRow, Integer pageSize,
                                        List<TableDataDto.Header> headerList, String sortBy, String sortingField) {
        if (StringUtils.isBlank(sortBy) && !StringUtils.isBlank(sortingField)) {
            throw new CustomException("sortingField存在,sortBy不能为空");
        }
        if (!StringUtils.isBlank(sortBy) && StringUtils.isBlank(sortingField)) {
            throw new CustomException("sortBy存在,,sortingField不能为空");
        }

        StringBuffer sql = new StringBuffer();
        String id = null;
        for (TableFieldInfo field : fields) {
            if (field.isPrimaryKey()) {
                id = field.getFieldName();
            }
            if (sql.length() == 0) {
                sql.append("select ");

            } else {
                sql.append(" , ");
            }
            sql.append("t_1.`").append(field.getFieldName()).append("`");

            // 头部信息
            if (headerList != null) {
                headerList.add(new TableDataDto.Header(field.getId(), field.getFieldName(), field.getComment(),
                        field.getDataType(), field.isPrimaryKey(), null, null, null, field.getDefaultShow()));
            }
        }

        if (StringUtils.isBlank(sql)) {
            sql.append("select * ");
        }
        sql.append(" from ").append(tableName).append(" t_1 ");

        sql.append(whereSql);

        if (!StringUtils.isBlank(sortBy) && !StringUtils.isBlank(sortingField)) {
            SortingFieldEnum sortingFieldEnum = SortingFieldEnum.valueOfCode(sortBy);
            if (sortingFieldEnum == null) {
                throw new CustomException("sortBy值不合法");
            }
            List<String> sortingFieldList = Arrays.stream(sortingField.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            sql.append(" ORDER BY ");
            sortingFieldList.forEach(sortField -> {
                sql.append("t_1.").append(sortField).append(" ");
                sql.append(sortBy);
                sql.append(", ");
            });

            sql.replace(sql.length() - 2, sql.length(), "");

        } else if (id != null) {
            sql.append(" ORDER BY t_1.");
            sql.append(id);
            sql.append(" DESC ");
        }
        if (startRow != null && pageSize != null) {
            sql.append(" LIMIT ");
            sql.append(startRow);
            sql.append(",");
            sql.append(pageSize);
        }

        return sql.toString();
    }

    public static String reverseSql(String sql) {
        return " NOT (" + sql + ") ";
    }

    /**
     * 条件取反转换回SQL
     * ( prodcode = 'P012' and tym ~ '氨基葡萄糖' ) or ( prodcode = 'P012' and jx ~ '灌肠剂|喷剂|软膏与乳膏剂|栓剂|贴剂|注射剂' ) or ( prodcode = 'P012' and sort3= '回收站') or ( prodcode = 'P012' and sort3= '解痉药')
     * 取反后，应该是(template.`t_1_jx` NOT LIKE '%灌肠剂%' AND template.`t_1_jx` NOT LIKE '%喷剂%' AND
     * template.`t_1_jx` NOT LIKE '%软膏与乳膏剂%' AND template.`t_1_jx` NOT LIKE '%栓剂%' AND
     * template.`t_1_jx` NOT LIKE '%贴剂%' AND template.`t_1_jx` NOT LIKE '%注射剂%')， 而不是OR连接复杂条件
     */
    public static String reverseSql2(String sql) {
        PGSQLStatementParser parser = new PGSQLStatementParser(query_prefix + sql);
        PGSelectStatement sqlStatement = parser.parseSelect();
        PGSelectQueryBlock query = (PGSelectQueryBlock) sqlStatement.getSelect().getQuery();

        SQLExpr where = query.getWhere();

        // 反置操作符
        reverseOpt(where);

        if (where instanceof SQLInListExpr) {
            String full = sqlStatement.toString();
            String condition = full.split("\n")[2];
            return condition.replaceAll(".*WHERE", "");
        }
//        log.info("result={}", where.toString());

        return where.toString();
    }

    static void reverseOpt(SQLExpr expr) {
        if (expr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr op = (SQLBinaryOpExpr) expr;
            SQLBinaryOperator curOpt = op.getOperator();
            SQLBinaryOperator tarOp = reverseMap.get(curOpt);
            if (Objects.nonNull(tarOp)) {
                op.setOperator(tarOp);
            } else {
                if (Objects.equals(curOpt, SQLBinaryOperator.BooleanAnd)) {
                    op.setOperator(SQLBinaryOperator.BooleanOr);
                    op.setBracket(true);
                } else if (Objects.equals(curOpt, SQLBinaryOperator.BooleanOr)) {
                    op.setOperator(SQLBinaryOperator.BooleanAnd);
                }
            }


            reverseOpt(op.getLeft());
            reverseOpt(op.getRight());
        } else if (expr instanceof SQLInListExpr) {
            SQLInListExpr op = (SQLInListExpr) expr;
            op.setNot(!op.isNot());
        }
    }

    public static String buildWhereSQL(FilterDTO filter) {
        if (filter == null) {
            return "";
        }
        // 构建 where 语句
        String whereSql = "";
        Table table = new Table();
        table.setUniqueId(1L);
        table.setFactTable(true);

        com.sinohealth.bi.data.Filter targetFilter = new com.sinohealth.bi.data.Filter();
        ApplicationSqlUtil.convertToFilter(filter, targetFilter);
        final ClickHouse mySql = new ClickHouse(Collections.singletonList(table), targetFilter);
        whereSql = mySql.getWhereSql();
        whereSql = whereSql.replace("t_1.", "").replace("WHERE", "");

        return whereSql;
    }

    /**
     * 华为云SQL导出项目
     * SELECT a.id, a.project_name ,a.template_id,t.template_name, a.applicant_id ,a.applicant_name,a.create_time,a.src_application_id ,a.data_expire, app.granularity_json
     * FROM tg_user_data_assets  a LEFT JOIN tg_template_info t on a.template_id  = t.id
     * left join tg_application_info app on app.id = a.src_application_id
     * where t.id in (100,102,103) and a.project_name not like '%test%' and a.require_time_type  =  2 and a.project_name not like '%测试%' and a.data_expire > now() and a.copy_from_id is null;
     */
    static void parseApplyInfo(String inPath, String outPath) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outPath))) {
            List<String> rows = Files.readAllLines(Paths.get(inPath));
            for (String row : rows) {
                // header
                if (row.contains("granularity_json")) {
                    writer.write(row + "\n");
                    continue;
                }
                String[] cols = row.split("\",\"");
                // 保留前 9 列
                String handle = Stream.of(cols).limit(9).collect(Collectors.joining("\",\""));
                writer.write(handle + "\"");
                String json = cols[cols.length - 1];
                json = json.replace("\"\"", "\"");
                List<ApplicationGranularityDto> list = JsonUtils.parseArray(json, ApplicationGranularityDto.class);
                if (Objects.isNull(list)) {
                    writer.write(",ERROR\n");
                    continue;
                }
//                log.info("={}", list.size());
                String timeGra = list.stream().filter(v -> Objects.equals(v.getGranularity(), FieldGranularityEnum.time.name()))
                        .flatMap(v -> v.getSelectGranularity().stream()).collect(Collectors.joining(";"));
                writer.write(",\"" + timeGra + "\"");

                String where = list.stream().filter(v -> Objects.equals(v.getGranularity(), FieldGranularityEnum.product.name()))
                        .map(v -> buildWhereSQL(v.getFilter()))
                        .map(v -> v.replace("`", ""))
                        .collect(Collectors.joining(";"));
                writer.write(",\"" + where + "\"\n");
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

}
