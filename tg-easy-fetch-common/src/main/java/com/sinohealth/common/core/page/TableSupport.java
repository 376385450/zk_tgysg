package com.sinohealth.common.core.page;

import cn.hutool.core.lang.Validator;
import com.github.pagehelper.PageHelper;
import com.sinohealth.common.utils.ServletUtils;
import com.sinohealth.common.utils.sql.SqlUtil;

/**
 * 表格数据处理
 *
 * @author dataplatform
 */
public class TableSupport {
    /**
     * 当前记录起始索引
     */
    public static final String PAGE_NUM = "pageNum";

    /**
     * 每页显示记录数
     */
    public static final String PAGE_SIZE = "pageSize";

    /**
     * 排序列
     */
    public static final String ORDER_BY_COLUMN = "orderByColumn";

    /**
     * 排序的方向 "desc" 或者 "asc".
     */
    public static final String IS_ASC = "isAsc";

    /**
     * 封装分页对象
     */
    public static PageDomain getPageDomain() {
        PageDomain pageDomain = new PageDomain();

        Object pageNum = ServletUtils.getRequestAttributes().getAttribute(PAGE_NUM, 0);
        int defaultPageNum = ServletUtils.getParameter(PAGE_NUM) == null ? 1 : ServletUtils.getParameterToInt(PAGE_NUM);
        int defaultPageSize = ServletUtils.getParameter(PAGE_SIZE) == null ? 10 : ServletUtils.getParameterToInt(PAGE_SIZE);
//        pageDomain.setPageNum(pageNum != null ? Integer.parseInt(pageNum.toString()) : defaultPageNum);
        pageDomain.setPageNum(defaultPageNum);
        pageDomain.setPageSize(defaultPageSize > 100 ? 100 : defaultPageSize);
        pageDomain.setOrderByColumn(ServletUtils.getParameter(ORDER_BY_COLUMN));
        pageDomain.setIsAsc(ServletUtils.getParameter(IS_ASC));
        return pageDomain;
    }


    public static void setPageSize(Integer pageNum, Integer pageSize) {
        ServletUtils.getRequestAttributes().setAttribute(PAGE_NUM, pageNum, 0);
        ServletUtils.getRequestAttributes().setAttribute(PAGE_SIZE, pageSize, 0);
    }

    public static PageDomain buildPageRequest() {
        return getPageDomain();
    }

    public static void startPage(String orderBy) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        if (Validator.isNotNull(pageNum) && Validator.isNotNull(pageSize)) {
            PageHelper.startPage(pageNum, pageSize, orderBy != null ? orderBy : SqlUtil.escapeOrderBySql(pageDomain.getOrderBy()));
        }
    }

    public static void startPage() {
        startPage(null);
    }
}
