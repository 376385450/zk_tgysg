package com.sinohealth;

import com.sinohealth.system.mapper.TgHiveProviderMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("gray")
@AllArgsConstructor
public class HiveTest {

    private final TgHiveProviderMapper tgHiveProviderMapper;

    @Test
    public void testLink(){
//        String result = tgHiveProviderMapper.showCreateTable("show create table hdfs_cmh_fd_data_sku_v3_shard");
//        System.out.println(result);
    }
}
