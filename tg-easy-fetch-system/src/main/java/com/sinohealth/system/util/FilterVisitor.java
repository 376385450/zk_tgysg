package com.sinohealth.system.util;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.druid.sql.visitor.VisitorFeature;
import com.alibaba.druid.util.JdbcConstants;
import com.sinohealth.system.dto.analysis.FilterDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-09-13 17:54
 */
@Slf4j
@Data
public class FilterVisitor extends SQLASTOutputVisitor {

    private FilterDTO root = new FilterDTO();

    public FilterVisitor() {
        super(new StringBuilder());
    }

    @Override
    public boolean visit(SQLBinaryOpExpr x) {
        SQLBinaryOperator operator = x.getOperator();
        if (this.parameterized
                && operator == SQLBinaryOperator.BooleanOr
                && !isEnabled(VisitorFeature.OutputParameterizedQuesUnMergeOr)) {
            x = SQLBinaryOpExpr.merge(this, x);

            operator = x.getOperator();
        }

        // TODO IN 重写
        if (inputParameters != null
                && !inputParameters.isEmpty()
                && operator == SQLBinaryOperator.Equality
                && x.getRight() instanceof SQLVariantRefExpr
        ) {
            SQLVariantRefExpr right = (SQLVariantRefExpr) x.getRight();
            int index = right.getIndex();
            if (index >= 0 && index < inputParameters.size()) {
                Object param = inputParameters.get(index);
                if (param instanceof Collection) {
                    x.getLeft().accept(this);
                    print0(" IN (");
                    right.accept(this);
                    print(')');
                    return false;
                }
            }
        }

        SQLObject parent = x.getParent();
        boolean isRoot = parent instanceof SQLSelectQueryBlock;
        boolean relational = operator == SQLBinaryOperator.BooleanAnd
                || operator == SQLBinaryOperator.BooleanOr;

        // 计算 SQL 片段 层级
        if (isRoot && relational) {
            this.indentCount++;
        }

        List<SQLExpr> groupList = new ArrayList<>();
        SQLExpr left = x.getLeft();
        SQLExpr right = x.getRight();

        if (inputParameters != null
                && operator != SQLBinaryOperator.Equality) {
            int varIndex = -1;
            if (right instanceof SQLVariantRefExpr) {
                varIndex = ((SQLVariantRefExpr) right).getIndex();
            }

            Object param = null;
            if (varIndex >= 0 && varIndex < inputParameters.size()) {
                param = inputParameters.get(varIndex);
            }

            if (param instanceof Collection) {
                Collection values  = (Collection) param;

                if (!values.isEmpty()) {
                    print('(');
                    int valIndex = 0;
                    for (Object value : values) {
                        if (valIndex++ != 0) {
                            print0(ucase ? " OR " : " or ");
                        }
                        printExpr(left);
                        print(' ');
                        if (operator == SQLBinaryOperator.Is) {
                            print('=');
                        } else {
                            printOperator(operator);
                        }
                        print(' ');
                        printParameter(value);
                    }
                    print(')');
                    return false;
                }
            }
        }

        if (operator.isRelational()
                && left instanceof SQLIntegerExpr
                && right instanceof SQLIntegerExpr) {
            print(((SQLIntegerExpr) left).getNumber().longValue());
            print(' ');
            printOperator(operator);
            print(' ');
            print(((SQLIntegerExpr) right).getNumber().longValue());
            return false;
        }

        for (;;) {
            if (left instanceof SQLBinaryOpExpr && ((SQLBinaryOpExpr) left).getOperator() == operator) {
                SQLBinaryOpExpr binaryLeft = (SQLBinaryOpExpr) left;
                groupList.add(binaryLeft.getRight());
                left = binaryLeft.getLeft();
            } else {
                groupList.add(left);
                break;
            }
        }

        for (int i = groupList.size() - 1; i >= 0; --i) {
            SQLExpr item = groupList.get(i);

            if (relational) {
                if (isPrettyFormat() && item.hasBeforeComment()) {
                    printlnComments(item.getBeforeCommentsDirect());
                }
            }

            if (isPrettyFormat() && item.hasBeforeComment()) {
                printlnComments(item.getBeforeCommentsDirect());
            }

            visitBinaryLeft(item, operator);

            if (isPrettyFormat() && item.hasAfterComment()) {
                print(' ');
                printlnComment(item.getAfterCommentsDirect());
            }

            if (i != groupList.size() - 1 && isPrettyFormat() && item.getParent().hasAfterComment()) {
                print(' ');
                printlnComment(item.getParent().getAfterCommentsDirect());
            }

            boolean printOpSpace = true;
            if (relational) {
                println();
            } else {
                if (operator == SQLBinaryOperator.Modulus
                        && JdbcConstants.ORACLE.equals(dbType)
                        && left instanceof SQLIdentifierExpr
                        && right instanceof SQLIdentifierExpr
                        && ((SQLIdentifierExpr) right).getName().equalsIgnoreCase("NOTFOUND")) {
                    printOpSpace = false;
                }
                if (printOpSpace) {
                    print(' ');
                }
            }
            printOperator(operator);
            if (printOpSpace) {
                print(' ');
            }
        }

        visitorBinaryRight(x);

        if (isRoot && relational) {
            this.indentCount--;
        }

        return false;
    }


    private void visitBinaryLeft(SQLExpr left, SQLBinaryOperator op) {
        if (left instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr binaryLeft = (SQLBinaryOpExpr) left;
            SQLBinaryOperator leftOp = binaryLeft.getOperator();
            boolean leftRational = leftOp == SQLBinaryOperator.BooleanAnd
                    || leftOp == SQLBinaryOperator.BooleanOr;

            if (leftOp.priority > op.priority
                    || (binaryLeft.isBracket()
                    && leftOp != op
                    && leftOp.isLogical()
                    && op.isLogical()
            )) {
                if (leftRational) {
                    this.indentCount++;
                }
                print('(');
                printExpr(left);
                print(')');

                if (leftRational) {
                    this.indentCount--;
                }
            } else {
                printExpr(left);
            }
        } else {
            printExpr(left);
        }
    }

    private void visitorBinaryRight(SQLBinaryOpExpr x) {
        if (isPrettyFormat() && x.getRight().hasBeforeComment()) {
            printlnComments(x.getRight().getBeforeCommentsDirect());
        }

        if (x.getRight() instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr right = (SQLBinaryOpExpr) x.getRight();
            SQLBinaryOperator rightOp = right.getOperator();
            SQLBinaryOperator op = x.getOperator();
            boolean rightRational = rightOp == SQLBinaryOperator.BooleanAnd
                    || rightOp == SQLBinaryOperator.BooleanOr;

            if (rightOp.priority >= op.priority
                    || (right.isBracket()
                    && rightOp != op
                    && rightOp.isLogical()
                    && op.isLogical()
            )) {
                if (rightRational) {
                    this.indentCount++;
                }

                print('(');
                printExpr(right);
                print(')');

                if (rightRational) {
                    this.indentCount--;
                }
            } else {
                printExpr(right);
            }
        } else {
            printExpr(x.getRight());
        }

        if (x.getRight().hasAfterComment() && isPrettyFormat()) {
            print(' ');
            printlnComment(x.getRight().getAfterCommentsDirect());
        }
    }
}
