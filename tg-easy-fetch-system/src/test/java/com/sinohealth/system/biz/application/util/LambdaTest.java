package com.sinohealth.system.biz.application.util;

import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-01 16:06
 */
@Slf4j
public class LambdaTest {

    @Test
    public void testBuildMap() throws Exception {

        List<UserDataAssets> list = Stream.of(1L, 2L).map(v -> {
            UserDataAssets u = new UserDataAssets();
            u.setId(v);
            return u;
        }).collect(Collectors.toList());

        Map<Long, Integer> versionMap = Lambda.buildMap(list, UserDataAssets::getId, UserDataAssets::getVersion);
        log.info("versionMap={}", versionMap);
    }
}