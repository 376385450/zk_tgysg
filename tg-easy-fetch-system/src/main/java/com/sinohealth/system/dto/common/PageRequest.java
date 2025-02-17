package com.sinohealth.system.dto.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-12-10 14:36
 * @see PageInfo
 */
@Data
public class PageRequest implements Serializable {
    @NotNull
    @ApiModelProperty(value = "页码", example = "1")
    private Integer page = 1;
    @NotNull
    @ApiModelProperty(value = "页面大小", example = "20")
    private Integer size = 20;

    public <T> IPage<T> buildPage() {
        return new Page<T>().setSize(this.size).setCurrent(this.page);
    }

    public static <T> IPage<T> buildPage(Integer page, Integer size) {
        return new Page<T>().setSize(size).setCurrent(page);
    }

    public Integer buildOffset() {
        return (page - 1) * size;
    }
}
