package com.sinohealth.system.biz.dataassets.constant;

import org.junit.Test;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-06-24 11:34
 */
public class AssetsQcTypeEnumTest {

    @Test
    public void testAllSql() throws Exception {
        System.out.println(AssetsQcTypeEnum.sku.buildInsert("cmh_enlarge_project_ysg_sku_gray_shard", "Test", "test_snap"));

    }
}
