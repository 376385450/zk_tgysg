package com.sinohealth.system.biz.ck.constant;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-12-06 17:24
 */
public interface CkTableSuffixTable {

    String LOCAL = "_local";

    String SHARD = "_shard";

    String SNAP = "_snap";

    String SWAP_SHARD = "_swap_shard";

    static String getOrigin(String shard) {
        return shard.replace(SHARD, "");
    }

    static String getSwapShard(String shard) {
        return shard.replace(SHARD, SWAP_SHARD);
    }

    static String getLocal(String shard) {
        return shard.replace(SHARD, LOCAL);
    }

    static String getShard(String local) {
        return local.replace(LOCAL, SHARD);
    }
}
