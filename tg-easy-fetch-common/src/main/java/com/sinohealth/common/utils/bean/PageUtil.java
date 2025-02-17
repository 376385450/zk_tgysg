package com.sinohealth.common.utils.bean;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-10 16:44
 */
public class PageUtil {

    public static <T> PageInfo<T> convert(IPage<T> page) {
        PageInfo<T> pageInfo = new PageInfo<>();
        pageInfo.setPages((int) page.getPages());
        pageInfo.setPageNum((int) page.getCurrent());
        pageInfo.setTotal(page.getTotal());
        pageInfo.setList(page.getRecords().stream().filter(Objects::nonNull).collect(Collectors.toList()));
        pageInfo.setSize((int) page.getSize());
        return pageInfo;
    }

    public static <T, R> PageInfo<R> convert(PageInfo<T> page, Function<T, R> func) {
        PageInfo<R> pageInfo = new PageInfo<>();
        pageInfo.setPages(page.getPages());
        pageInfo.setPageNum(page.getPageNum());
        pageInfo.setTotal(page.getTotal());
        pageInfo.setList(page.getList().stream().filter(Objects::nonNull).map(func).collect(Collectors.toList()));
        pageInfo.setSize(page.getSize());
        return pageInfo;
    }

    public static <T, R> PageInfo<R> convert(IPage<T> page, Function<T, R> func) {
        PageInfo<R> pageInfo = new PageInfo<>();
        pageInfo.setPages((int) page.getPages());
        pageInfo.setPageNum((int) page.getCurrent());
        pageInfo.setTotal(page.getTotal());
        pageInfo.setList(page.getRecords().stream().filter(Objects::nonNull).map(func).collect(Collectors.toList()));
        pageInfo.setSize((int) page.getSize());
        return pageInfo;
    }

    public static <T, R> IPage<R> convertMap(IPage<T> page, Function<T, R> func) {
        Page<R> pageInfo = new Page<>();
        pageInfo.setPages(page.getPages());
        pageInfo.setCurrent(page.getCurrent());
        pageInfo.setTotal(page.getTotal());
        pageInfo.setSize(page.getSize());
        pageInfo.setRecords(page.getRecords().stream().filter(Objects::nonNull).map(func).collect(Collectors.toList()));
        return pageInfo;
    }

    public static <T> IPage<T> empty() {
        Page<T> pageInfo = new Page<>();
        pageInfo.setPages(0);
        pageInfo.setCurrent(1);
        pageInfo.setTotal(0);
        pageInfo.setSize(0);
        pageInfo.setRecords(Collections.emptyList());
        return pageInfo;
    }
}
