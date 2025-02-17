package com.sinohealth.common.core.domain;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming
public class PageModel<T> implements Serializable {

    public PageModel(IPage<T> page) {
        this.pages = page.getPages();
        this.total = page.getTotal();
        this.current = page.getCurrent();
        this.result = page.getRecords();
    }

    public PageModel(IPage page, List<T> records) {
        this.pages = page.getPages();
        this.total = page.getTotal();
        this.current = page.getCurrent();
        this.result = records;
    }

    private Long pages;

    private Long current;

    private Long total;

    private List<T> result;

}
