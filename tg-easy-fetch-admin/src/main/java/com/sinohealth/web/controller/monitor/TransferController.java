package com.sinohealth.web.controller.monitor;

import com.alibaba.excel.util.BooleanUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.application.util.WideTableSqlBuilder;
import com.sinohealth.system.biz.dataassets.dao.AssetsWideUpgradeTriggerDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsSnapshotDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsWideUpgradeTrigger;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsSnapshot;
import com.sinohealth.system.biz.template.dao.TemplateInfoDAO;
import com.sinohealth.system.biz.transfer.service.A650ProjectImporter;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.TgTemplatePackTailSetting;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.mapper.TgTemplatePackTailSettingMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 线下项目迁移，完整流程：业务方填配置表，依次分类导入Excel数据（项目，需求），验证资产数据 GP和CK差异
 *
 * @author Kuangcp
 * 2025-01-07 14:52
 * @see TaskController#batchParseSql 宽表解析
 */
@Slf4j
@RestController
@RequestMapping({"/task/trans", "/api/task/trans"})
public class TransferController {

    @Autowired
    private TemplateInfoDAO templateInfoDAO;
    @Autowired
    private A650ProjectImporter a650ProjectImporter;
    @Autowired
    private TgTemplatePackTailSettingMapper packTailSettingMapper;
    @Autowired
    private ApplicationDAO applicationDAO;
    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private UserDataAssetsSnapshotDAO userDataAssetsSnapshotDAO;
    @Autowired
    private AssetsWideUpgradeTriggerDAO wideUpgradeTriggerDAO;


    // 注意 先导入成功所有 项目后，才处理需求，避免数据残缺
    @PostMapping("/importProject")
    public String import650Project(@RequestParam("file") MultipartFile file) {
        a650ProjectImporter.parseProject(file);
        return "OK";
    }

    // 宽表 （普通+长尾）
    @PostMapping("/importWideApply")
    public String importWideApply(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        a650ProjectImporter.parseWideApply(file, request);
        return "OK";
    }

    // 工作流常规
    @PostMapping("/importFlowApply")
    public String importFlowApply(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        a650ProjectImporter.parseFlowApply(file, request);
        return "OK";
    }

    // 自定义列 工作流-常规
    @PostMapping("/importRangeApply")
    public String importRangeApply(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        a650ProjectImporter.parseRangeApply(file, request);
        return "OK";
    }

    // 通用 无明细
    @PostMapping("/importInComApply")
    public String importInComApply(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        a650ProjectImporter.parseInCompleteCustomApply(file, request);
        return "OK";
    }

    // 通用
    @PostMapping("/importComFlowApply")
    public String importComFlowApply(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        a650ProjectImporter.parseCustomFlowApply(file, request);
        return "OK";
    }

    /**
     * 迁移原模板下所有的申请和资产到新模板 CMH_城市_单品
     * <p>
     * CMH_城市_单品(常规长尾)	打包方式：打包到分类四
     * CMH_城市_单品(特殊长尾_打包到通用名)	打包方式：打包到通用名
     *
     * @see TaskController#fixTransApplyError 类似修数据
     */
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/mergeTailTemplate")
    public String mergeTailTemplate(@RequestParam(value = "mod", required = false) Boolean mod) {
        // 100
        String target = "CMH_城市_单品";
        // 152 153
        List<String> origin = Arrays.asList("CMH_城市_单品(常规长尾)", "CMH_城市_单品(特殊长尾_打包到通用名)");
        Long targetId;
        List<Long> originIds;

        Map<String, String> packMap = new HashMap<>();
        packMap.put("CMH_城市_单品(常规长尾)", "打包到分类四");
        packMap.put("CMH_城市_单品(特殊长尾_打包到通用名)", "打包到通用名");

        List<String> tmp = new ArrayList<>();
        tmp.addAll(origin);
        tmp.add(target);
        List<TgTemplateInfo> tempList = templateInfoDAO.lambdaQuery()
                .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName)
                .in(TgTemplateInfo::getTemplateName, tmp)
                .list();
        Map<String, Long> nameIdMap = Lambda.buildMap(tempList, TgTemplateInfo::getTemplateName, TgTemplateInfo::getId);
        Map<Long, String> idNameMap = Lambda.buildMap(tempList, TgTemplateInfo::getId, TgTemplateInfo::getTemplateName);

        targetId = nameIdMap.get(target);
        originIds = nameIdMap.entrySet().stream().filter(v -> origin.contains(v.getKey()))
                .map(Map.Entry::getValue).collect(Collectors.toList());

        List<TgTemplatePackTailSetting> settings = packTailSettingMapper
                .selectList(new QueryWrapper<TgTemplatePackTailSetting>().lambda()
                        .eq(TgTemplatePackTailSetting::getTemplateId, targetId)
                );
        Map<String, TgTemplatePackTailSetting> setMap = Lambda.buildMap(settings, TgTemplatePackTailSetting::getName);
        boolean match = setMap.keySet().containsAll(packMap.values());
        if (!match) {
            throw new CustomException("缺失打包配置");
        }

        // 补充打包配置
        List<UserDataAssets> assets = userDataAssetsDAO.lambdaQuery()
                .select(UserDataAssets::getSrcApplicationId)
                .in(UserDataAssets::getTemplateId, originIds)
                .notIn(UserDataAssets::getSrcApplicationId, Collections.singletonList(6443L))
                .gt(UserDataAssets::getDataExpire, LocalDateTime.now())
                .list();
        List<Long> applyIds = Lambda.buildList(assets, UserDataAssets::getSrcApplicationId);
        if (CollectionUtils.isEmpty(applyIds)) {
            return "EMPTY apply";
        }
        List<TgApplicationInfo> applyList = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getAssetsId, TgApplicationInfo::getTemplateId)
                .in(TgApplicationInfo::getId, applyIds)
                .list();
        if (CollectionUtils.isEmpty(applyList)) {
            return "Empty";
        }

        boolean modify = BooleanUtils.isTrue(mod);
        Map<Long, List<TgApplicationInfo>> typeApplyMap = applyList.stream()
                .collect(Collectors.groupingBy(TgApplicationInfo::getTemplateId));
        for (Map.Entry<Long, List<TgApplicationInfo>> entry : typeApplyMap.entrySet()) {
            Set<Long> ids = Lambda.buildSet(entry.getValue(), TgApplicationInfo::getId);
            log.warn("{}: {}", entry.getKey(), ids);
            TgTemplatePackTailSetting setting = Optional.ofNullable(idNameMap.get(entry.getKey()))
                    .map(packMap::get).map(setMap::get).orElseThrow(() -> new CustomException("打包配置缺失: " + entry.getKey()));

            if (modify) {
                applicationDAO.lambdaUpdate()
                        .set(TgApplicationInfo::getPackTailSwitch, true)
                        .set(TgApplicationInfo::getPackTailId, setting.getId())
                        .set(TgApplicationInfo::getPackTailName, setting.getName())
                        .set(TgApplicationInfo::getTemplateId, targetId)
                        .in(TgApplicationInfo::getId, ids)
                        .update();
            }
        }

        // 更换模板
        Set<Long> aids = Lambda.buildSet(applyList, TgApplicationInfo::getAssetsId);
        if (modify) {
            userDataAssetsDAO.lambdaUpdate()
                    .in(UserDataAssets::getId, aids)
                    .set(UserDataAssets::getTemplateId, targetId)
                    .update();
            userDataAssetsSnapshotDAO.lambdaUpdate()
                    .in(UserDataAssetsSnapshot::getAssetsId, aids)
                    .set(UserDataAssetsSnapshot::getTemplateId, targetId)
                    .update();
        }

        // 更新出数SQL
        // 通过对比SQL是否有差异 来确认迁移的效果
        Set<Long> ids = Lambda.buildSet(applyList, TgApplicationInfo::getId);
        List<TgApplicationInfo> allApply = applicationDAO.listByIds(ids);

        int errCnt = 0;
        for (TgApplicationInfo apply : allApply) {
            JsonBeanConverter.convert2Obj(apply);
            Long applyId = apply.getId();

            String origin1 = apply.getAsql();
            String origin2 = apply.getTailSql();

            boolean fail = false;
            AjaxResult<TgApplicationInfo> applyResult = this.extractSql(apply);
            if (!applyResult.isSuccess()) {
                log.warn("{}: {}", applyId, applyResult);
                errCnt++;
                continue;
            }

            if (!Objects.equals(apply.getAsql(), origin1)) {
                log.info("{} has apply diff\n{}\n{}", applyId, origin1, apply.getAsql());
                errCnt++;
                fail = true;
            }
            if (!Objects.equals(apply.getTailSql(), origin2)) {
                log.info("{} has tail diff\n{}\n{}", applyId, origin2, apply.getTailSql());
                errCnt++;
                fail = true;
            }

            if (modify && !fail) {
                applicationDAO.lambdaUpdate()
                        .set(TgApplicationInfo::getAsql, apply.getAsql())
                        .set(TgApplicationInfo::getTailSql, apply.getTailSql())
                        .eq(TgApplicationInfo::getId, applyId)
                        .update();
            }
        }
        log.info("size={} errCnt={}", allApply.size(), errCnt);

        // 资产升级
        if (modify) {
            List<AssetsWideUpgradeTrigger> list = allApply.stream().map(v -> {
                AssetsWideUpgradeTrigger trigger = new AssetsWideUpgradeTrigger();
                trigger.setTableId(v.getBaseTableId());
                trigger.setActVersion(46);
                trigger.setAssetsId(v.getAssetsId());
                trigger.setApplyId(v.getId());
                trigger.setState(AssetsUpgradeStateEnum.wait.name());
                return trigger;
            }).collect(Collectors.toList());
            wideUpgradeTriggerDAO.saveBatch(list);
        }

        return "OK";
    }

    public AjaxResult<TgApplicationInfo> extractSql(TgApplicationInfo applicationInfo) {
        boolean fillResult = new WideTableSqlBuilder().fillApplication(applicationInfo);
        if (!fillResult) {
            return AjaxResult.error("申请失败，请反馈技术人员处理");
        }
        JsonBeanConverter.convert2Obj(applicationInfo);
        return AjaxResult.success(applicationInfo);
    }
}
