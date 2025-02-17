package com.sinohealth.system.util;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectStatement;
import com.alibaba.druid.sql.dialect.postgresql.parser.PGSQLStatementParser;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinohealth.bi.constant.SqlConstant;
import com.sinohealth.bi.data.ClickHouse;
import com.sinohealth.bi.data.Filter;
import com.sinohealth.bi.data.Table;
import com.sinohealth.bi.enums.FunctionalOperatorEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.poi.ExcelUtil;
import com.sinohealth.sca.base.basic.util.DateUtils;
import com.sinohealth.system.biz.application.dto.HistoryProject;
import com.sinohealth.system.biz.project.util.DataPlanUtil;
import com.sinohealth.system.dto.analysis.FilterDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.Assert;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-09-07 21:15
 */
@Slf4j
public class HistoryApplyUtil {

    public static final String prefix = "SELECT * FROM a WHERE ";

    public static String format(Object obj) {
        try {
            return new ObjectMapper()
                    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 只有宽表需要如此处理
     *
     * @param endTime YM格式
     */
    public static Date parseExpire(String endTime) {
        LocalDate endDate;
        try {
            endDate = YearMonth.parse(endTime, DataPlanUtil.YM)
                    .atDay(1).plusMonths(1).minusDays(1);
//                if (endDate.isBefore(now)) {
//                    endDate = now;
//                }
        } catch (Exception e) {
            log.error("", e);
            throw new CustomException(e.getMessage());
        }

        return Date.from(LocalDateTime.of(endDate.plusMonths(3), LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * @param sql GP语法
     * @return 后端FilterDTO 不含father节点
     * 注意 条件SQL中不能出现别名
     */
    public static FilterDTO parseSql(String sql) {
        log.info("sql: {}", sql);
        if (!sql.contains("select") && !sql.contains("SELECT")) {
            sql = prefix + sql;
        }

        // 品牌工作流模板
        sql = sql.replace("<>", "!=");
        sql = sql.replace("! ~", " !~ ");
        sql = sql.replace(" ", " ");

        SQLExpr where = printFmtSql(sql);

        FilterDTO filter = new FilterDTO();
        fill(where, filter);

        return filter;
    }

    /**
     * @see ApplicationSqlUtil#reverseSql 简洁可靠
     * 条件取反转换回SQL
     * ( prodcode = 'P012' and tym ~ '氨基葡萄糖' ) or ( prodcode = 'P012' and jx ~ '灌肠剂|喷剂|软膏与乳膏剂|栓剂|贴剂|注射剂' ) or ( prodcode = 'P012' and sort3= '回收站') or ( prodcode = 'P012' and sort3= '解痉药')
     * 取反后，应该是(template.`t_1_jx` NOT LIKE '%灌肠剂%' AND template.`t_1_jx` NOT LIKE '%喷剂%' AND
     * template.`t_1_jx` NOT LIKE '%软膏与乳膏剂%' AND template.`t_1_jx` NOT LIKE '%栓剂%' AND
     * template.`t_1_jx` NOT LIKE '%贴剂%' AND template.`t_1_jx` NOT LIKE '%注射剂%')， 而不是OR连接复杂条件
     */
    public static String convertReverseSql(String sql) {
        PGSQLStatementParser parser = new PGSQLStatementParser(prefix + sql);
        PGSelectStatement sqlStatement = parser.parseSelect();
        PGSelectQueryBlock query = (PGSelectQueryBlock) sqlStatement.getSelect().getQuery();

        SQLExpr where = query.getWhere();

        // 反置操作符
        reverseOpt(where);

        if (where instanceof SQLInListExpr) {
            String full = sqlStatement.toString();
            String condition = full.split("\n")[2];
            // TODO 当是 NOT like 不能把 AND 替换成OR
            return reverseAndOr(condition.replaceAll(".*WHERE", ""));
        }
//        log.info("result={}", where.toString());

        return reverseAndOr(where.toString());
    }

    public static String convertFuzzyQuerySql(String sql, List<String> excludeFields) {
        // 把 = 转成 !=
        PGSQLStatementParser parser = new PGSQLStatementParser(prefix + sql);
        PGSelectStatement sqlStatement = parser.parseSelect();
        PGSelectQueryBlock query = (PGSelectQueryBlock) sqlStatement.getSelect().getQuery();

        SQLExpr where = query.getWhere();

        convertFuzzyQuery(where, excludeFields);

        if (where instanceof SQLInListExpr) {
            String full = sqlStatement.toString();
            String condition = full.split("WHERE")[1];
            return condition;
        }

        return where.toString();
    }

    static void convertFuzzyQuery(SQLExpr expr, List<String> excludeFields) {
        if (expr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr op = (SQLBinaryOpExpr) expr;
            if (op.getLeft() instanceof SQLIdentifierExpr) {
                if (CollUtil.isNotEmpty(excludeFields) && excludeFields.contains(((SQLIdentifierExpr) op.getLeft()).getName())) {
                    // 排除部分字段
                    return;
                }

                if (Objects.equals(op.getOperator(), SQLBinaryOperator.Equality)) {
                    op.setOperator(SQLBinaryOperator.Like);
                    final SQLCharExpr right = (SQLCharExpr) op.getRight();
                    right.setText("%" + right.getText() + "%");
                } else if (Objects.equals(op.getOperator(), SQLBinaryOperator.NotEqual)) {
                    op.setOperator(SQLBinaryOperator.NotLike);
                    final SQLCharExpr right = (SQLCharExpr) op.getRight();
                    right.setText("%" + right.getText() + "%");
                }
                return;
            }

            convertFuzzyQuery(op.getLeft(), excludeFields);
            convertFuzzyQuery(op.getRight(), excludeFields);
        } else if (expr instanceof SQLInListExpr) {
            // in ('','') 转换为 like '%%' or like '%%'
            // not in ('','') 转换成 not like '%%' and not like '%%'
            final String name = ((SQLIdentifierExpr) ((SQLInListExpr) expr).getExpr()).getName();
            if (excludeFields.contains(name)) {
                return;
            }
            List<SQLBinaryOpExpr> likeList = new ArrayList<>();
            final boolean not = ((SQLInListExpr) expr).isNot();
            final List<SQLExpr> targetList = ((SQLInListExpr) expr).getTargetList();
            SQLBinaryOperator operator = SQLBinaryOperator.BooleanOr;
            for (SQLExpr sqlExpr : targetList) {
                SQLBinaryOpExpr op = new SQLBinaryOpExpr();
                op.setLeft(new SQLIdentifierExpr(name));
                if (not) {
                    operator = SQLBinaryOperator.BooleanAnd;
                    op.setOperator(SQLBinaryOperator.NotLike);
                } else {
                    op.setOperator(SQLBinaryOperator.Like);
                }
                op.setRight(new SQLIdentifierExpr("'%" + ((SQLCharExpr) sqlExpr).getText() + "%'"));
                likeList.add(op);
            }
            SQLBinaryOpExpr binaryOpExpr = new SQLBinaryOpExpr();
            binaryOpExpr = fillChild(binaryOpExpr, likeList, operator, 0);
            if (expr.getParent() instanceof PGSelectQueryBlock) {
                ((PGSelectQueryBlock) expr.getParent()).setWhere(binaryOpExpr);
            } else {
                SQLBinaryOpExpr opExpr = (SQLBinaryOpExpr) expr.getParent();
                if (opExpr.getRight() == expr) {
                    opExpr.setRight(binaryOpExpr);
                } else {
                    opExpr.setLeft(binaryOpExpr);
                }
            }
            return;
        }
    }

    static String reverseAndOr(String sql) {
        sql = sql.replace("AND", "\uD83D\uDC20");
        sql = sql.replace("OR", "\uD83D\uDC19");

        sql = sql.replace("\uD83D\uDC20", "OR");
        sql = sql.replace("\uD83D\uDC19", "AND");

        return sql;
    }

    static SQLBinaryOpExpr fillChild(SQLBinaryOpExpr op, List<SQLBinaryOpExpr> ops, SQLBinaryOperator operator, Integer index) {
        int i = index++;
        if (ops.size() == 1) {
            return ops.get(0);
        }
        // 终止条件: 处理最后两个节点
        if (i == ops.size() - 2) {
            op.setRight(ops.get(i));
            op.setOperator(operator);
            op.setLeft(ops.get(i + 1));
            return op;
        } else {
            op.setRight(ops.get(i));
            op.setOperator(operator);
            final SQLBinaryOpExpr left = new SQLBinaryOpExpr();
            op.setLeft(fillChild(left, ops, operator, index));
            return op;
        }
    }


    static void reverseOpt(SQLExpr expr) {
        if (expr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr op = (SQLBinaryOpExpr) expr;
            if (op.getLeft() instanceof SQLIdentifierExpr) {
                if (Objects.equals(op.getOperator(), SQLBinaryOperator.Equality)) {
                    op.setOperator(SQLBinaryOperator.NotEqual);
                } else if (Objects.equals(op.getOperator(), SQLBinaryOperator.NotEqual)) {
                    op.setOperator(SQLBinaryOperator.Equality);
                } else if (Objects.equals(op.getOperator(), SQLBinaryOperator.Like)) {
                    op.setOperator(SQLBinaryOperator.NotLike);
                } else if (Objects.equals(op.getOperator(), SQLBinaryOperator.POSIX_Regular_Match)) {
                    op.setOperator(SQLBinaryOperator.POSIX_Regular_Not_Match);
                }
                return;
            }

            reverseOpt(op.getLeft());
            reverseOpt(op.getRight());
        } else if (expr instanceof SQLInListExpr) {
            SQLInListExpr op = (SQLInListExpr) expr;
            op.setNot(!op.isNot());
        }
    }

    public static FilterDTO parseByVisitor(String sql) {
        log.info("sql: {}", sql);
        if (!sql.contains("select") && !sql.contains("SELECT")) {
            sql = prefix + sql;
        }
        SQLExpr where = printFmtSql(sql);

        FilterVisitor visitor = new FilterVisitor();
        where.accept(visitor);

        return visitor.getRoot();
    }

    public static SQLExpr printFmtSql(String sql) {
        try {
            PGSQLStatementParser parser = new PGSQLStatementParser(sql);
            PGSelectStatement sqlStatement = parser.parseSelect();
            PGSelectQueryBlock query = (PGSelectQueryBlock) sqlStatement.getSelect().getQuery();

            SQLExpr where = query.getWhere();
            // TODO debug
//        log.info("format:\n\n {}", where.toString());
            return where;
        } catch (Exception e) {
            log.error("{} ", sql, e);
            throw e;
        }
    }

    /**
     * @param filter 前端传入的Filter （有father节点）
     */
    public static String buildSql(FilterDTO filter) {
        Filter targetFilter = new Filter();
//        ApplicationSqlUtil.fillFilterV2(filter, targetFilter);
        ApplicationSqlUtil.convertToFilter(filter, targetFilter);

        Table table = new Table();
        table.setUniqueId(1L);
        table.setFactTable(true);
        final ClickHouse mySql = new ClickHouse(Collections.singletonList(table), targetFilter);
        String whereSql = mySql.getWhereSql();
        if (Objects.isNull(whereSql)) {
            log.error("NO SQL");
            return null;
        }

        whereSql = whereSql.replace("t_null.", "");
//        log.info("CK SQL: {}", whereSql);
        whereSql = whereSql.replace("`", "");

        log.warn("SQL: {}\n", whereSql);
        return whereSql;
    }

    static FunctionalOperatorEnum convertOperator(SQLBinaryOperator operator) {
        switch (operator) {
            case POSIX_Regular_Match:
            case Like:
                return FunctionalOperatorEnum.CONTAIN;
            case POSIX_Regular_Not_Match:
            case NotLike:
                return FunctionalOperatorEnum.NOT_CONTAIN;
            case Equality:
                return FunctionalOperatorEnum.EQUAL_TO;
            case NotEqual:
                return FunctionalOperatorEnum.NOT_EQUAL_TO;
            case LessThan:
                return FunctionalOperatorEnum.LESS_THAN;
            case LessThanOrEqual:
                return FunctionalOperatorEnum.LESS_THAN_OR_EQUAL_TO;
            case GreaterThan:
                return FunctionalOperatorEnum.MORE_THAN;
            case GreaterThanOrEqual:
                return FunctionalOperatorEnum.MORE_THAN_OR_EQUAL_TO;
            default:
                throw new RuntimeException("不支持的运算符 " + operator);
        }
    }

    /**
     * in 和 not in 单节点可完成解析
     */
    static void fillLeaf(SQLInListExpr expr, FilterDTO filter) {
        SQLExpr field = expr.getExpr();
        if (field instanceof SQLIdentifierExpr) {
            FilterDTO.FilterItemDTO item = new FilterDTO.FilterItemDTO();
            SQLIdentifierExpr base = (SQLIdentifierExpr) field;

            String and = parseOperator(expr.getParent());
            item.setAndOr(and);
            filter.setLogicalOperator(and);

            item.setFieldName(base.getName().toLowerCase());
            item.setFunctionalOperator(expr.isNot() ? FunctionalOperatorEnum.NOT_EQUAL_TO.getType() : FunctionalOperatorEnum.EQUAL_TO.getType());

            List<SQLExpr> items = expr.getTargetList();
            String value = items.stream().map(Object::toString).map(v -> {
                if (v.startsWith("'") && v.endsWith("'")) {
                    return v.substring(1, v.length() - 1);
                }
                return v;
            }).collect(Collectors.joining(SqlConstant.IN_OR));
            item.setValue(value);
            filter.setFilterItem(item);
        } else {
            throw new RuntimeException("sql IN (): no value");
        }
    }

    /**
     * 双节点组成 条件
     * <p>
     * 单值处理：等于，like
     */
    static boolean fillBiLeaf(SQLBinaryOpExpr expr, FilterDTO filter) {
        SQLExpr left = expr.getLeft();
        SQLExpr right = expr.getRight();

        // 叶子节点
        if (left instanceof SQLIdentifierExpr) {
            FilterDTO.FilterItemDTO item = new FilterDTO.FilterItemDTO();
            SQLIdentifierExpr base = (SQLIdentifierExpr) left;

            String and = parseOperator(expr.getParent());

            item.setAndOr(and);
            filter.setLogicalOperator(and);

            item.setFieldName(base.getName().toLowerCase());
            item.setFunctionalOperator(convertOperator(expr.getOperator()).getType());
            if (right instanceof SQLIntegerExpr) {
                item.setValue(((SQLIntegerExpr) right).getValue());
            } else {
                String v = right.toString();
                if (v.startsWith("'") && v.endsWith("'")) {
                    v = v.substring(1, v.length() - 1);
                }
                if (v.startsWith("%") && v.endsWith("%")) {
                    v = v.substring(1, v.length() - 1);
                }
                item.setValue(v);
            }
            filter.setFilterItem(item);
            return true;
        }
        return false;
    }


    static String parseOperator(SQLObject parent) {
        // 根节点，只有一个条件的情况
        if (parent instanceof PGSelectQueryBlock) {
            return "and";
        }

        SQLBinaryOperator operator = ((SQLBinaryOpExpr) parent).getOperator();
        return parseOperator(operator);
    }

    static String parseOperator(SQLBinaryOperator operator) {
        return Objects.equals(operator, SQLBinaryOperator.BooleanAnd) ? "and" : "or";
    }

    static void fillOpNode(SQLBinaryOpExpr expr, FilterDTO filter) {
        boolean bracket = expr.isBracket();
        filter.setBracket(bracket);

        boolean isLeaf = fillBiLeaf(expr, filter);
        if (isLeaf) {
            return;
        }

        SQLExpr left = expr.getLeft();
        SQLExpr right = expr.getRight();

        String and = parseOperator(expr.getOperator());
        filter.setLogicalOperator(and);
        List<FilterDTO> childes = new ArrayList<>();
        filter.setFilters(childes);

        FilterDTO leftFilter = new FilterDTO();
        FilterDTO rightFilter = new FilterDTO();
        boolean fill = fill(left, leftFilter);
        if (!fill) {
            throw new RuntimeException("不支持的类型");
        }
        fill = fill(right, rightFilter);
        if (!fill) {
            throw new RuntimeException("不支持的类型");
        }


        boolean leftBracket = left instanceof SQLBinaryOpExpr && ((SQLBinaryOpExpr) left).isBracket();
        boolean rightBracket = right instanceof SQLBinaryOpExpr && ((SQLBinaryOpExpr) right).isBracket();
        // 将下层节点 提高到当前层
        if (!leftBracket) {
            handleSon(leftFilter, childes);
        } else {
            childes.add(leftFilter);
        }
        if (!rightBracket) {
            handleSon(rightFilter, childes);
        } else {
            childes.add(rightFilter);
        }

    }

    private static void handleSon(FilterDTO sonFilter, List<FilterDTO> childes) {
        List<FilterDTO> nextSon = sonFilter.getFilters();
        if (Objects.isNull(nextSon)) {
            childes.add(sonFilter);
            return;
        }

        // 子节点都是括号，自己不是
        boolean sonAllBracket = nextSon.stream().allMatch(v -> BooleanUtils.isTrue(v.getBracket()));
        if (sonAllBracket && BooleanUtils.isFalse(sonFilter.getBracket())) {
            childes.addAll(nextSon);
            return;
        }

        // 将子节点不是括号的，提高一层
        List<FilterDTO> leafNodes = nextSon.stream()
                .filter(v -> BooleanUtils.isNotTrue(v.getBracket())).collect(Collectors.toList());
        childes.addAll(leafNodes);
        nextSon.removeAll(leafNodes);
        if (CollectionUtils.isNotEmpty(nextSon)) {
            childes.add(sonFilter);
        }
    }

    /**
     * 处理容器节点
     */
    static void fillOpNode2(SQLBinaryOpExpr expr, FilterDTO filter) {
        filter.setBracket(expr.isBracket());

        boolean isLeaf = fillBiLeaf(expr, filter);
        if (isLeaf) {
            return;
        }

        SQLExpr left = expr.getLeft();
        SQLExpr right = expr.getRight();

        String and = parseOperator(expr.getOperator());
        filter.setLogicalOperator(and);
        List<FilterDTO> childes = new ArrayList<>();
        filter.setFilters(childes);

        FilterDTO leftFilter = new FilterDTO();
        FilterDTO rightFilter = new FilterDTO();
        boolean fill = fill(left, leftFilter);
        if (!fill) {
            throw new RuntimeException("不支持的类型");
        }
        fill = fill(right, rightFilter);
        if (!fill) {
            throw new RuntimeException("不支持的类型");
        }

        boolean bracket = expr.isBracket();

        // 左右节点是否有括号
        Boolean leftBra = Optional.of(left).filter(v -> v instanceof SQLBinaryOpExpr).map(v -> (SQLBinaryOpExpr) v)
                .map(SQLBinaryOpExpr::isBracket).orElse(false);
        Boolean rightBra = Optional.of(right).filter(v -> v instanceof SQLBinaryOpExpr).map(v -> (SQLBinaryOpExpr) v)
                .map(SQLBinaryOpExpr::isBracket).orElse(false);

        boolean leftOp = left instanceof SQLBinaryOpExpr;
        boolean rightOp = right instanceof SQLBinaryOpExpr;


        Boolean leftLeaf = Optional.of(left).filter(v -> v instanceof SQLBinaryOpExpr).map(v -> (SQLBinaryOpExpr) v)
                .map(v -> {
                    SQLExpr leftSon = v.getLeft();
                    return !(leftSon instanceof SQLBinaryOpExpr);
                }).orElse(false);
        Boolean rightLeaf = Optional.of(right).filter(v -> v instanceof SQLBinaryOpExpr).map(v -> (SQLBinaryOpExpr) v)
                .map(v -> {
                    SQLExpr leftSon = v.getLeft();
                    return !(leftSon instanceof SQLBinaryOpExpr);
                }).orElse(false);

        // 当前节点有括号，且下层 左右节点没括号， 且下层两个节点都是容器节点 时
        boolean judgeV1 = bracket && leftOp && rightOp && !leftBra && !rightBra;

        // 当前节点有括号，且下层 左右节点没括号， 且下层两个节点都是容器节点 且 （ 左右节点子节点是叶子节点 且 不是根节点） 或者 （左右节点子节点不是叶子节点） 时
        boolean root = expr.getParent() instanceof PGSelectQueryBlock;

        // 自身，左右都是容器
        boolean currentContainer = leftOp && rightOp;

        boolean judgeV2 = bracket && currentContainer && ((!root && leftLeaf && rightLeaf) || (!leftLeaf && !rightLeaf));
//        boolean judgeV2 = bracket && leftOp && rightOp && !root  && !leftLeaf && !rightLeaf;

        // 调试，不做提取时的结构
//        judgeV2 = false;

        boolean removeContainer = currentContainer
//                && !bracket
//                && !root
                && !((SQLBinaryOpExpr) left).isBracket()
                && !((SQLBinaryOpExpr) right).isBracket();

        // 将下层节点 提高到当前层
        if (judgeV2 || removeContainer) {
            boolean nest = hasNestBracket((SQLBinaryOpExpr) left);
            List<FilterDTO> c1son = leftFilter.getFilters();
            if ((nest || bracket) && !root && CollectionUtils.isNotEmpty(c1son)) {
                childes.addAll(c1son);
            } else {
                childes.add(leftFilter);
            }

            // 为了解决最后一个条件 括号被去掉
            nest = hasNestBracket((SQLBinaryOpExpr) right);
            List<FilterDTO> c2son = rightFilter.getFilters();
            if (nest && CollectionUtils.isNotEmpty(c2son)) {
                childes.addAll(c2son);
            } else {
                childes.add(rightFilter);
            }
        } else {
            childes.add(leftFilter);
            childes.add(rightFilter);
        }
    }

    static boolean hasNestBracket(SQLBinaryOpExpr node) {
        return Stream.of(node.getLeft(), node.getRight()).anyMatch(v -> {
            if (v instanceof SQLBinaryOpExpr) {
                SQLBinaryOpExpr t = (SQLBinaryOpExpr) v;
                if (t.isBracket()) {
                    return true;
                }
                return hasNestBracket(t);
            }
            return false;
        });
    }

    /**
     * 递归解析子节点 包括叶子节点
     */
    static boolean fill(SQLExpr expr, FilterDTO filter) {
        if (expr instanceof SQLBinaryOpExpr) {
            fillOpNode((SQLBinaryOpExpr) expr, filter);
            return true;
        } else if (expr instanceof SQLInListExpr) {
            fillLeaf((SQLInListExpr) expr, filter);
            return true;
        } else {
            return false;
        }
    }

    static FilterDTO parse(HistoryProject project) {
        Assert.assertNotNull(project);
        String exclude = convertReverseSql(project.getExcludeCondition());
        return parseSql("(" + project.getProjectCondition() + ") AND (" + exclude + ")");
    }

    static void writeResult(List<HistoryProject> projects) {
        ExcelUtil<HistoryProject> excelUtil = new ExcelUtil<>(HistoryProject.class);
        excelUtil.exportExcel(projects, "SQL",
                "解析-" + new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date()) + ".xlsx");
    }

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(Duration.ofMillis(500))
            .callTimeout(Duration.ofMillis(500))
            .connectTimeout(Duration.ofMillis(500))
            .build();


    @Data
    private static class FilterReq implements Serializable {
        private FilterDTO filter;
    }

    @Data
    private static class FilterResult {
        private Integer code;
        private FilterDTO data;
    }

    public static Optional<FilterDTO> parseForFront(String sql, String url) {
        FilterDTO filter = parseSql(sql);
        return fillFatherNode(filter, url);
    }

    public static Optional<FilterDTO> fillFatherNode(FilterDTO filterDTO, String url) {
        if (Objects.isNull(filterDTO)) {
            return Optional.empty();
        }
        FilterReq req = new FilterReq();
        if (CollectionUtils.isEmpty(filterDTO.getFilters()) && Objects.nonNull(filterDTO.getFilterItem())) {
            FilterDTO dto = new FilterDTO();
            ArrayList<FilterDTO> s = new ArrayList<>();
            s.add(filterDTO);
            dto.setFilters(s);
            req.filter = dto;
        } else {
            req.filter = filterDTO;
        }
        // 转换filter
        String format = JsonUtils.format(req);
        if (Objects.isNull(format)) {
            return Optional.empty();
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), format);
        try (Response resp = client.newCall(new Request.Builder().url(url).post(body).build()).execute()) {
            String rs = resp.body().string();
            log.info("req={}", format);
            log.info("resp={}", rs);
            FilterResult result = JsonUtils.parse(rs, FilterResult.class);
            return Optional.ofNullable(result).map(v -> v.data);
        } catch (Exception e) {
            log.error("", e);
        }
        return Optional.empty();
    }

    public static String convertPeriod(String origin) {
        return convertCommonPeriod(origin, true);
    }

    public static String convertCommonPeriod(String origin, boolean flow) {
        int first = origin.indexOf("and");
        String end = origin.substring(first + 4);
        if (end.contains("<=")) {
            int l = end.indexOf("'");
            int r = end.lastIndexOf("'");
            String prefix = end.substring(0, l);
            String date = end.substring(l + 1, r);
            SimpleDateFormat yyyyMM = new SimpleDateFormat("yyyy-MM");
            SimpleDateFormat tf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date parse = yyyyMM.parse(date);
                Date newDate = DateUtils.addMonths(parse, 1);
                prefix = prefix.replace("<=", "<");
                end = prefix + "'" + tf.format(newDate) + "'";
            } catch (Exception e) {
                log.error("", e);
            }

        } else {
            int last = end.lastIndexOf("'");
            end = end.substring(0, last) + "-01'";
        }
        String startPart = origin.substring(0, first).trim();
        int val = startPart.lastIndexOf("'");
        String start = startPart.substring(0, val) + "-01' and ";

        String finalSQL = start + end;
        if (flow && !finalSQL.contains("period_new")) {
            finalSQL = finalSQL.replace("period", "period_new");
        }
        return finalSQL;
    }
}
