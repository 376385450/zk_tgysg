package com.sinohealth.system.biz.application.util;

import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.application.dto.TgUserDataAssetsDistDto;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.dto.auditprocess.ProcessNodeEasyDto;

import java.util.Objects;
import java.util.Optional;

/**
 * 申请单工具类
 *
 * @author Kuangcp
 * 2024-12-11 15:38
 */
public class ApplyUtil {

    /**
     * 分隔符 粒度信息
     */
    public static final String GRA_SPLIT = "__";

    public static boolean containGra(String gra, String search) {
        if (StringUtils.isBlank(gra)) {
            return false;
        }
        return gra.contains(GRA_SPLIT + search + GRA_SPLIT);
    }

    public static void cleanGra(TgUserDataAssetsDistDto dto) {
        cleanGra(dto.getProductGra()).ifPresent(dto::setProductGra);
        cleanGra(dto.getTimeGra()).ifPresent(dto::setTimeGra);
    }

    public static Optional<String> cleanGra(String str) {
        return Optional.ofNullable(str)
                .map(v -> v.substring(GRA_SPLIT.length(), v.length() - GRA_SPLIT.length()))
                .map(v -> v.replace(GRA_SPLIT, ","));
    }

    public static Optional<ProcessNodeEasyDto> lastNode(TgApplicationInfo application) {
        int lastIdx = application.getCurrentIndex() - 1;
        if (application.getCurrentIndex() == 0 && application.getHandleNode().size() == 1) {
            lastIdx = 0;
        }
        int endIdx = application.getHandleNode().size() - 1;
        if (application.getCurrentIndex() == endIdx
                && !Objects.equals(ApplicationConst.AuditStatus.AUDITING, application.getHandleNode().get(endIdx).getStatus())) {
            lastIdx = application.getCurrentIndex();
        }
        if (lastIdx >= 0) {
            ProcessNodeEasyDto lastNode = application.getHandleNode().get(lastIdx);
            return Optional.ofNullable(lastNode);
        }
        return Optional.empty();
    }


    public static Optional<ProcessNodeEasyDto> lastNode(TgUserDataAssetsDistDto application) {
        int lastIdx = application.getCurrentIndex() - 1;
        if (application.getCurrentIndex() == 0 && application.getHandleNode().size() == 1) {
            lastIdx = 0;
        }
        int endIdx = application.getHandleNode().size() - 1;
        if (application.getCurrentIndex() == endIdx
                && !Objects.equals(ApplicationConst.AuditStatus.AUDITING, application.getHandleNode().get(endIdx).getStatus())) {
            lastIdx = application.getCurrentIndex();
        }
        if (lastIdx >= 0) {
            ProcessNodeEasyDto lastNode = application.getHandleNode().get(lastIdx);
            return Optional.ofNullable(lastNode);
        }
        return Optional.empty();
    }

    public static String userName(String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }

        String[] arr = name.split("-");
        return arr[arr.length - 1];
    }
}
