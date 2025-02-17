package com.sinohealth.system.biz.dir.entity;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-18 15:48
 */
public interface DisplaySort {


    Long getId();

    Integer getDisSort();

    void fillDisSort(Integer sort);

    String getName();


    String SORT_FIELD = "dis_sort";

    String ID_FIELD = "id";
}
