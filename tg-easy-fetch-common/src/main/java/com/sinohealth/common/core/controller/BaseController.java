package com.sinohealth.common.core.controller;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.PageDomain;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.common.core.page.TableSupport;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.sql.SqlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * web层通用数据处理
 *
 * @author dataplatform
 */
public class BaseController {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 将前台传递过来的日期格式的字符串，自动转化为Date类型
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    /**
     * 设置请求分页数据
     */
    protected void startPage() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        if (Validator.isNotNull(pageNum) && Validator.isNotNull(pageSize)) {
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.startPage(pageNum, pageSize, orderBy);
        }
    }

    protected void startPage(String orderBy) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        if (Validator.isNotNull(pageNum) && Validator.isNotNull(pageSize)) {
            PageHelper.startPage(pageNum, pageSize, orderBy);
        }
    }

    protected <T> PageInfo<T> list2PageInfo(List<T> arrayList, Integer pageNum, Integer pageSize) {
        //实现list分页
        PageHelper.startPage(pageNum, pageSize);
        int pageStart = pageNum == 1 ? 0 : (pageNum - 1) * pageSize;
        int pageEnd = arrayList.size() < pageSize * pageNum ? arrayList.size() : pageSize * pageNum;
        List<T> pageResult = new LinkedList<T>();
        if (arrayList.size() > pageStart) {
            pageResult = arrayList.subList(pageStart, pageEnd);
        }
        PageInfo<T> pageInfo = new PageInfo<T>(pageResult);
        //获取PageInfo其他参数
        pageInfo.setTotal(arrayList.size());
        int endRow = (int) pageInfo.getEndRow() == 0 ? 0 : (int) ((pageNum - 1) * pageSize + pageInfo.getEndRow() + 1);
        pageInfo.setEndRow(endRow);
        boolean hasNextPage = arrayList.size() <= pageSize * pageNum ? false : true;
        pageInfo.setHasNextPage(hasNextPage);
        boolean hasPreviousPage = pageNum == 1 ? false : true;
        pageInfo.setHasPreviousPage(hasPreviousPage);
        pageInfo.setIsFirstPage(!hasPreviousPage);
        boolean isLastPage = (arrayList.size() > pageSize * (pageNum - 1) && arrayList.size() <= pageSize * pageNum) ? true : false;
        pageInfo.setIsLastPage(isLastPage);
        int pages = arrayList.size() % pageSize == 0 ? arrayList.size() / pageSize : (arrayList.size() / pageSize) + 1;
        pageInfo.setNavigateLastPage(pages);
        int[] navigatePageNums = new int[pages];
        for (int i = 1; i < pages; i++) {
            navigatePageNums[i - 1] = i;
        }
        pageInfo.setNavigatepageNums(navigatePageNums);
        int nextPage = pageNum < pages ? pageNum + 1 : 0;
        pageInfo.setNextPage(nextPage);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setPages(pages);
        pageInfo.setPrePage(pageNum - 1);
        pageInfo.setSize(pageInfo.getList().size());
        int starRow = arrayList.size() < pageSize * pageNum ? 1 + pageSize * (pageNum - 1) : 0;
        pageInfo.setStartRow(starRow);
        return pageInfo;
    }

    /**
     * 响应请求分页数据
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected <T> TableDataInfo<T> getDataTable(List<T> list) {
        TableDataInfo<T> rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(new PageInfo(list).getTotal());
        return rspData;
    }

    /**
     * 响应返回结果
     *
     * @param rows 影响行数
     * @return 操作结果
     */
    protected AjaxResult<Object> toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 响应返回结果
     *
     * @param result 结果
     * @return 操作结果user.setCreateBy(SecurityUtils.getUsername ());
     *         user.setPassword(SecurityUtils.encryptPassword(dto.getPassword()));
     *         user.setToken(UUID.fastUUID().toString());
     *         userService.insertUser(user, dto.getRoleIds(), dto.getGroupIds(), dto.getTree(), dto.getMenus());
     *         return toAjax(1);
     */
    protected AjaxResult<Object> toAjax(boolean result) {
        return result ? success() : error();
    }

    /**
     * 返回成功
     */
    public AjaxResult<Object> success() {
        return AjaxResult.success();
    }

    /**
     * 返回失败消息
     * @return
     */
    public AjaxResult<Object> error() {
        return AjaxResult.error();
    }

    /**
     * 返回成功消息
     */
    public AjaxResult<Object> success(String message) {
        return AjaxResult.success(message);
    }

    /**
     * 返回失败消息
     * @return
     */
    public AjaxResult<Object> error(String message) {
        return AjaxResult.error(message);
    }

    /**
     * 页面跳转
     */
    public String redirect(String url) {
        return StrUtil.format("redirect:{}", url);
    }
}
