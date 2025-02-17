package com.sinohealth.system.lineage.common;

import com.sinohealth.system.lineage.druid.LineageUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LineageDetail {

    private String sql = null;
    private List<String> selectColumnList = new ArrayList<>();
    private List<String> dbNameList = new ArrayList<>();
    private List<String> tableNameList = new ArrayList<>();
    private List<String> columnNameList = new ArrayList<>();

    public LineageDetail(String sql) {
        this.sql = sql;
        LineageColumn root = new LineageColumn();
        TreeNode<LineageColumn> rootNode = new TreeNode<>(root);
        LineageUtils.columnLineageAnalyzer(this.sql, rootNode);

        for (TreeNode<LineageColumn> e : rootNode.getChildren()) {
            Set<LineageColumn> leafNodes = e.getAllLeafData();
            this.selectColumnList.add(e.getData().getTargetColumnName());

            for (LineageColumn f : leafNodes) {
                if (f.getIsEnd()) {
                    if (f.getTargetColumnName() != null) {
                        this.columnNameList.add(f.getTargetColumnName());
                    }

                    if (f.getSourceDbName() != null) {
                        this.dbNameList.add(f.getSourceDbName());
                    }

                    if (f.getSourceTableName() != null) {
                        this.tableNameList.add(f.getSourceTableName());
                    }

                    //                    System.out.println(e.getData().getTargetColumnName() + "\tfrom:"+ JSONObject.toJSONString(f)+"\n");
                }
            }
        }

        this.columnNameList = new ArrayList<>(new HashSet<>(this.columnNameList));
        this.dbNameList = new ArrayList<>(new HashSet<>(this.dbNameList));
        this.tableNameList = new ArrayList<>(new HashSet<>(this.tableNameList));
    }

    public int getSelectColumnNumber() {
        return this.selectColumnList.size();
    }

    public List<String> getDbNameList() {
        return dbNameList;
    }

    public List<String> getTableNameList() {
        return tableNameList;
    }

    public List<String> getColumnNameList() {
        return columnNameList;
    }
}
