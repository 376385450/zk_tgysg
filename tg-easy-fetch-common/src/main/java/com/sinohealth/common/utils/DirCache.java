package com.sinohealth.common.utils;

import com.sinohealth.common.core.domain.entity.DataDir;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jingjun
 * @since 2021/5/12
 */
public class DirCache {

    private static List<DataDir> cache;

    public static void init(List<DataDir> list) {
        list.stream().filter(d -> d.getParentId().equals(0L)).forEach(d -> {
            d.setDirPath(d.getDirName());
            d.setIdPath(d.getId().toString());
            initPath(list, d);
        });
        cache = list;
    }

    public static void initPath(List<DataDir> list, DataDir parent) {
        List<DataDir> childList = list.stream().filter(d -> d.getParentId().equals(parent.getId())).collect(Collectors.toList());
        if (childList.isEmpty()) {
            parent.setEnd(true);
            return;
        }

        childList.forEach(d -> {
            d.setDirPath(parent.getDirPath() + ">" + d.getDirName());
            d.setIdPath(parent.getIdPath() + "," + d.getId());
            initPath(list, d);
        });
    }


    public static List<DataDir> getList() {
        return ImmutableList.copyOf(cache);
    }

    public static DataDir getDir(Long dirId) {
        return cache.stream().filter(d -> d.getId().equals(dirId)).findFirst().orElse(new DataDir());
    }

    public static List<Long>  getEndDirIds(Long dirId) {
        List<Long> dirIds=new ArrayList<>();

        if(getDir(dirId).isEnd()){
            dirIds.add(dirId);
        }else{
            findEndDir(dirId, dirIds);
        }
        return dirIds;
    }

    private static void findEndDir(Long dirId, List<Long> dirIds) {
        cache.stream().filter(d -> d.getParentId().equals(dirId)).forEach(u->{
            if(u.isEnd()){
                dirIds.add(u.getId());
            }else {
                findEndDir(u.getId(), dirIds);
            }
        });
    }
}
