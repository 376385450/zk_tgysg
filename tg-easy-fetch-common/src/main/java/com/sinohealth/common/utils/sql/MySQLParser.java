package com.sinohealth.common.utils.sql;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.odps.ast.OdpsValuesTableSource;
import com.alibaba.druid.stat.TableStat;
import org.springframework.beans.BeanUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL解析工具类
 *
 * @author linkaiwei
 * @date 2021/8/4 12:03
 * @since 1.1
 */
public class MySQLParser extends SQLUtils {

    private MySQLParser() {
    }

    /**
     * 获取SQL解析的字段信息
     *
     * @param sql SQL语句
     * @return 字段信息
     * @author linkaiwei
     * @date 2021-08-04 12:11:01
     * @since 1.1
     */
    public static Collection<ExportMySqlSchemaStatVisitor.ColumnAlias> getColumnList(String sql) {
        if (sql == null || sql.trim().length() < 1) {
            return null;
        }

        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLStatement statement = parser.parseStatement();

        ExportMySqlSchemaStatVisitor visitor = new ExportMySqlSchemaStatVisitor();
        statement.accept(visitor);

        return visitor.getColumnList();
    }


    /**
     * 自定义MySQL的SQL解析类
     *
     * @author linkaiwei
     * @date 2021/08/04 12:03
     * @since 1.1
     */
    public static class ExportMySqlSchemaStatVisitor extends MySqlSchemaStatVisitor {

        protected final Map<Long, ColumnAlias> columnList = new LinkedHashMap<>();

        public Collection<ColumnAlias> getColumnList() {
            return columnList.values();
        }

        /**
         * 自定义别名
         */
        public static class ColumnAlias extends TableStat.Column {

            /**
             * 字段别名
             */
            private String alias;

            /**
             * 表别名
             */
            private String tableAlias;


            public ColumnAlias(String table, String name) {
                super(table, name);
            }

            public ColumnAlias(String table, String name, long hashCode64) {
                super(table, name, hashCode64);
            }

            @Override
            public String getTable() {
                return super.getTable();
            }

            @Override
            public String getName() {
                return super.getName();
            }

            public String getAlias() {
                return alias;
            }

            public void setAlias(String alias) {
                this.alias = alias;
            }

            public String getTableAlias() {
                return tableAlias;
            }

            public void setTableAlias(String tableAlias) {
                this.tableAlias = tableAlias;
            }
        }


        @Override
        public boolean visit(MySqlSelectQueryBlock x) {
            final boolean visit = this.visit((SQLSelectQueryBlock) x);

            // 自定义设置字段别名
            final List<SQLSelectItem> selectList = x.getSelectList();
            columnList.forEach((aLong, columnAlias) -> selectList.forEach(sqlSelectItem -> {
                if (sqlSelectItem.getExpr() instanceof SQLPropertyExpr
                        && ((SQLPropertyExpr) sqlSelectItem.getExpr()).getOwnernName().equals(
                        columnAlias.getTableAlias())) {
                    columnAlias.setAlias(sqlSelectItem.getAlias());
                }
            }));

            return visit;
        }

        @Override
        public boolean visit(SQLPropertyExpr x) {
            TableStat.Column column = null;
            String ident = x.getName();

            SQLTableSource tableSource = x.getResolvedTableSource();
            if (tableSource instanceof SQLExprTableSource) {
                SQLExpr expr = ((SQLExprTableSource) tableSource).getExpr();

                if (expr instanceof SQLIdentifierExpr) {
                    SQLIdentifierExpr table = (SQLIdentifierExpr) expr;
                    SQLTableSource resolvedTableSource = table.getResolvedTableSource();
                    if (resolvedTableSource instanceof SQLExprTableSource) {
                        expr = ((SQLExprTableSource) resolvedTableSource).getExpr();
                    }
                } else if (expr instanceof SQLPropertyExpr) {
                    SQLPropertyExpr table = (SQLPropertyExpr) expr;
                    SQLTableSource resolvedTableSource = table.getResolvedTableSource();
                    if (resolvedTableSource instanceof SQLExprTableSource) {
                        expr = ((SQLExprTableSource) resolvedTableSource).getExpr();
                    }
                }

                if (expr instanceof SQLIdentifierExpr) {
                    SQLIdentifierExpr table = (SQLIdentifierExpr) expr;

                    SQLTableSource resolvedTableSource = table.getResolvedTableSource();
                    if (resolvedTableSource instanceof SQLWithSubqueryClause.Entry) {
                        return false;
                    }

                    column = addColumn(table.getName(), ident);

                    if (column != null && isParentGroupBy(x)) {
                        this.groupByColumns.add(column);
                    }
                } else if (expr instanceof SQLPropertyExpr) {
                    SQLPropertyExpr table = (SQLPropertyExpr) expr;
                    String tableName = table.toString();
                    column = addColumn(tableName, ident);

                    if (column != null && isParentGroupBy(x)) {
                        this.groupByColumns.add(column);
                    }
                } else if (expr instanceof SQLMethodInvokeExpr) {
                    SQLMethodInvokeExpr methodInvokeExpr = (SQLMethodInvokeExpr) expr;
                    if ("table".equalsIgnoreCase(methodInvokeExpr.getMethodName())
                            && methodInvokeExpr.getParameters().size() == 1
                            && methodInvokeExpr.getParameters().get(0) instanceof SQLName) {
                        SQLName table = (SQLName) methodInvokeExpr.getParameters().get(0);

                        String tableName = null;
                        if (table instanceof SQLPropertyExpr) {
                            SQLPropertyExpr propertyExpr = (SQLPropertyExpr) table;
                            SQLIdentifierExpr owner = (SQLIdentifierExpr) propertyExpr.getOwner();
                            if (propertyExpr.getResolvedTableSource() != null
                                    && propertyExpr.getResolvedTableSource() instanceof SQLExprTableSource) {
                                SQLExpr resolveExpr = ((SQLExprTableSource) propertyExpr.getResolvedTableSource()).getExpr();
                                if (resolveExpr instanceof SQLName) {
                                    tableName = resolveExpr.toString() + "." + propertyExpr.getName();
                                }
                            }
                        }

                        if (tableName == null) {
                            tableName = table.toString();
                        }

                        column = addColumn(tableName, ident);
                    }
                }
            } else if (tableSource instanceof SQLWithSubqueryClause.Entry
                    || tableSource instanceof SQLSubqueryTableSource
                    || tableSource instanceof SQLUnionQueryTableSource
                    || tableSource instanceof SQLLateralViewTableSource
                    || tableSource instanceof SQLValuesTableSource) {
                return false;
            } else {
                if (x.getResolvedProcudure() != null) {
                    return false;
                }

                if (x.getResolvedOwnerObject() instanceof SQLParameter) {
                    return false;
                }

                boolean skip = false;
                for (SQLObject parent = x.getParent(); parent != null; parent = parent.getParent()) {
                    if (parent instanceof SQLSelectQueryBlock) {
                        SQLTableSource from = ((SQLSelectQueryBlock) parent).getFrom();

                        if (from instanceof OdpsValuesTableSource) {
                            skip = true;
                            break;
                        }
                    } else if (parent instanceof SQLSelectQuery) {
                        break;
                    }
                }
                if (!skip) {
                    column = handleUnkownColumn(ident);
                }
            }

            if (column != null) {
                SQLObject parent = x.getParent();
                if (parent instanceof SQLSelectOrderByItem) {
                    parent = parent.getParent();
                }
                if (parent instanceof SQLPrimaryKey) {
                    column.setPrimaryKey(true);
                } else if (parent instanceof SQLUnique) {
                    column.setUnique(true);
                }

                setColumn(x, column);


                // 自定义设置表别名
                ColumnAlias columnAlias = new ColumnAlias(column.getTable(), column.getName(), column.hashCode64());
                BeanUtils.copyProperties(column, columnAlias);
                columnAlias.setTableAlias(x.getOwnernName());
                columnList.put(column.hashCode64(), columnAlias);
            }

            return false;
        }

        @Override
        protected TableStat.Column addColumn(String tableName, String columnName) {
            TableStat.Column column = this.getColumn(tableName, columnName);
            if (column == null && columnName != null) {
                column = new TableStat.Column(tableName, columnName);
                columns.put(column.hashCode64(), column);
            }
            return column;
        }

        private boolean isParentGroupBy(SQLObject parent) {
            for (; parent != null; parent = parent.getParent()) {
                if (parent instanceof SQLSelectItem) {
                    return false;
                }

                if (parent instanceof SQLSelectGroupByClause) {
                    return true;
                }
            }
            return false;
        }

        private void setColumn(SQLExpr x, TableStat.Column column) {
            SQLObject current = x;
            for (; ; ) {
                SQLObject parent = current.getParent();

                if (parent == null) {
                    break;
                }

                if (parent instanceof SQLSelectQueryBlock) {
                    SQLSelectQueryBlock query = (SQLSelectQueryBlock) parent;
                    if (query.getWhere() == current) {
                        column.setWhere(true);
                    }
                    break;
                }

                if (parent instanceof SQLSelectGroupByClause) {
                    SQLSelectGroupByClause groupBy = (SQLSelectGroupByClause) parent;
                    if (current == groupBy.getHaving()) {
                        column.setHaving(true);
                    } else if (groupBy.getItems().contains(current)) {
                        column.setGroupBy(true);
                    }
                    break;
                }

                if (isParentSelectItem(parent)) {
                    column.setSelec(true);
                    break;
                }

                if (parent instanceof SQLJoinTableSource) {
                    SQLJoinTableSource join = (SQLJoinTableSource) parent;
                    if (join.getCondition() == current) {
                        column.setJoin(true);
                    }
                    break;
                }

                current = parent;
            }
        }

        private boolean isParentSelectItem(SQLObject parent) {
            for (; parent != null; parent = parent.getParent()) {
                if (parent instanceof SQLSelectItem) {
                    return true;
                }

                if (parent instanceof SQLSelectQueryBlock) {
                    return false;
                }
            }
            return false;
        }
    }

}
