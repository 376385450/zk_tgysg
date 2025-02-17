package com.sinohealth.system.biz.monitor.adapter;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sinohealth.common.config.TransferProperties;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.ck.adapter.CKClusterAdapter;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.dto.compare.*;
import com.sinohealth.system.biz.dataassets.mapper.UserDataAssetsMapper;
import com.sinohealth.system.biz.monitor.dto.SortString;
import com.sinohealth.system.biz.monitor.dto.Table;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.mapper.DataDirMapper;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.mapper.TgCkProviderMapper;
import com.sinohealth.system.mapper.TgNodeMappingMapper;
import com.sinohealth.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.sinohealth.common.core.redis.RedisKeys.Apply.TRANS_APPLY_MAP;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-30 11:42
 */
@Slf4j
@Service
public class CompareAdapter {

    @Autowired
    private TransferProperties transferProperties;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TgApplicationInfoMapper applicationInfoMapper;
    @Autowired
    private UserDataAssetsMapper userDataAssetsMapper;
    @Autowired
    UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private DataDirMapper dataDirMapper;
    @Autowired
    private TgNodeMappingMapper nodeMappingMapper;

    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private CKClusterAdapter ckClusterAdapter;
    @Autowired
    private TgCkProviderMapper ckProviderMapper;

    /**
     * CMH品牌 查看差异结果
     *
     * @see this#compareAssetsForCmhFlow 计算差异
     */
    public String cmhFlowCompareList(Set<Long> ignoreIds) {
        StringBuilder style = new StringBuilder();

        style.append("table, th, td {\n" +
                "  border: 1px solid gainsboro;\n" +
                "}");
        StringBuilder body = new StringBuilder();

        int pass = 0;
        int noPass = 0;

        List<SortString> allProjects = new ArrayList<>();

        Table table = new Table();
        table.addHeader("序号", "ID", "项目名", "GP", "CK", "申请", "SzPhl", "JqPhl",
                "FdXse", "FdXsl", "AvgDj", "FdXseCw", "FdXslCw", "DjCw"
        );


        Map<Integer, String> proMap = new HashMap<>();
        List<AtomicInteger> headerCount = table.getHeader().stream().map(v -> new AtomicInteger()).collect(Collectors.toList());
        try {
            Map applyMap = redisTemplate.opsForHash().entries(RedisKeys.FlowApply.TRANS_APPLY_MAP);
            applyMap.forEach((key, value) -> {
                int id = Integer.parseInt(key.toString());
                TgApplicationInfo info = applicationInfoMapper.selectOne(new QueryWrapper<TgApplicationInfo>().lambda()
                        .select(TgApplicationInfo::getProjectName)
                        .eq(TgApplicationInfo::getId, Long.parseLong(value.toString())));
                Optional.ofNullable(info).map(TgApplicationInfo::getProjectName).ifPresent(v -> proMap.put(id, v));
            });
            List values = redisTemplate.opsForHash().values(RedisKeys.FlowApply.VALIDATE_RESULT_MAP);
            for (Object value : values) {
                CmhFlowCompareResultVO compare = JsonUtils.parse(value.toString(), CmhFlowCompareResultVO.class);
                boolean valid = BooleanUtils.isTrue(compare.getResult());
                if (valid) {
                    pass++;
                } else {
                    noPass++;
                }
                if (CollectionUtils.isNotEmpty(ignoreIds) && ignoreIds.contains(compare.getGp().getId().longValue())) {
                    continue;
                }

                compare.colsDiff();
                CmhFlowCompareVO gp = compare.getGp();
                CmhFlowCompareVO ck = compare.getCk();
                String proName = proMap.getOrDefault(gp.getId(), gp.getId() + "");
                Object lastApply = redisTemplate.opsForHash().get(RedisKeys.FlowApply.TRANS_APPLY_MAP, gp.getId());
                String applyId = Optional.ofNullable(lastApply).map(Object::toString).orElse("NULL");
                ArrayList<String> row = new ArrayList<>();
                String color = valid ? "green" : "red";
                row.add("<span style=\"color:" + color + ";\">" + gp.getId() + "</span>");
                row.add(proName.toString());
                row.add(gp.getTotal() + "");

                if (compare.diffTotal()) {
                    row.add("<span style=\"color:red;\">" + ck.getTotal() + "</span>");
                    headerCount.get(4).incrementAndGet();
                } else {
                    row.add(ck.getTotal() + "");
                }
                row.add("<a href=\"/tg-easy-fetch/task/applyConfig/" + applyId + "\">Config</a>");
                List<String> cols = compare.colsDiff();
                for (int i = 0; i < cols.size(); i++) {
                    String cel = cols.get(i);
                    if (!Objects.equals(cel, AbsCompareResultVO.none)) {
                        headerCount.get(i + 6).incrementAndGet();
                    }
                    row.add("<span style=\"font-size:12px;\">" + cel + "</span>");
                }
//                compare.colsDiff().stream().map(v -> "<span style=\"font-size:12px;\">" + v + "</span>").forEach(row::add);
                SortString ss = new SortString(gp.getId(), row);
                allProjects.add(ss);
            }
        } catch (Exception e) {
            log.error("", e);
            return "ERROR";
        }

        body.append(String.format("CMH 品牌 通过(绿色): %s 未通过(红色): %s 总计 %s<br>", pass, noPass, pass + noPass));
        body.append(" <h2> 项目明细 </h2> ");

        table.setHeaderDetail(headerCount.stream().map(v -> v.get() > 0 ? "<span style=\"color:red;\">" + v.get() + "</span>" : "").collect(Collectors.toList()));

        allProjects.stream().sorted(Comparator.comparing(SortString::getSort))
                .forEach(v -> table.addRow(v.getCols()));
        body.append(table.renderHtml());
        return "<html><head><style>" + style + "</style></head><body>" + body + "</body></html>";
    }

    /**
     * SKU 差异结果
     *
     * @see this#compareForSku 计算项目基础数据
     */
    public String skuResultList(String listKey, String resultKey, Set<Long> careIds) {
        StringBuilder result = new StringBuilder();

        int pass = 0;
        int noPass = 0;

        List<SortString> allProjects = new ArrayList<>();

        Table table = new Table();
        table.addHeader("序号", "ID", "项目名", "GP", "CK", "申请单", "AvgDj", "FdXse", "SzPhl",
                "JqPhl", "Xse", "Xsl", "Ddu", "Tv", "Vpd");
        Map proMap = redisTemplate.opsForHash().entries(RedisKeys.Apply.TRANS_PRO_MAP);
        try {
            List values = redisTemplate.opsForHash().values(resultKey);
            for (Object value : values) {
                CompareResultVO compare = JsonUtils.parse(value.toString(), CompareResultVO.class);
                if (CollectionUtils.isNotEmpty(careIds) && !careIds.contains(compare.getCk().getId())) {
                    continue;
                }
                boolean valid = BooleanUtils.isTrue(compare.getResult());
                if (valid) {
                    pass++;
                } else {
                    noPass++;
                }
                compare.checkDataDiff();
                CompareAssetsVO gp = compare.getGp();
                CompareAssetsVO ck = compare.getCk();
                Object proName = proMap.getOrDefault(gp.getId(), gp.getId());
                Object lastApply = redisTemplate.opsForHash().get(listKey, gp.getId());
                String applyId = Optional.ofNullable(lastApply).map(Object::toString).orElse("NULL");
                ArrayList<String> row = new ArrayList<>();
                String color = valid ? "green" : "red";
                row.add("<span style=\"color:" + color + ";\">" + gp.getId() + "</span>");
                row.add(proName.toString());
                row.add(gp.getTotal() + "");

                if (compare.diffTotal()) {
                    row.add("<span style=\"color:red;\">" + ck.getTotal() + "</span>");
                } else {
                    row.add(ck.getTotal() + "");
                }
                row.add("<a href=\"/tg-easy-fetch/task/applySQL/" + applyId + "\">SQL</a>");
                compare.colsDiff().stream().map(v -> "<span style=\"font-size:12px;\">" + v + "</span>").forEach(row::add);
//                row.addAll();
                SortString ss = new SortString(gp.getId(), row);
                allProjects.add(ss);
            }
        } catch (Exception e) {
            log.error("", e);
            return "ERROR";
        }

        if (Objects.equals(listKey, RedisKeys.Apply.TRANS_APPLY_MAP)) {
            result.append("CMH 常规 ");
        } else {
            result.append("CMH 长尾 ");
        }
        result.append(String.format("通过(绿色): %s 未通过(红色): %s 总计 %s <br>", pass, noPass, pass + noPass));

        result.append(" <h2> 项目明细 </h2> ");
        allProjects.stream().sorted(Comparator.comparing(SortString::getSort))
                .forEach(v -> table.addRow(v.getCols()));
        result.append(table.renderHtml());
        return result.toString();
    }

    public String mostError(String plainText) {
        StringBuilder result = new StringBuilder();

        int pass = 0;
        int noPass = 0;

        List<SortString> noDatas = new ArrayList<>();
        List<SortString> diffDatas = new ArrayList<>();

        Map proMap = redisTemplate.opsForHash().entries(RedisKeys.Apply.TRANS_PRO_MAP);
        Map<String, Integer> cmap = new HashMap<>();
        // 字段 -> 索引 -> 项目id
        Map<String, Map<Integer, List<Long>>> fieldMaps = new HashMap<>();
        // 项目id -> 字段
        Map<Long, Set<String>> proFieldMap = new HashMap<>();
        try {
            List values = redisTemplate.opsForHash().values(RedisKeys.Apply.VALIDATE_RESULT_MAP);
            for (Object value : values) {
                CompareResultVO compare = JsonUtils.parse(value.toString(), CompareResultVO.class);
//                if (!needIds.contains(compare.getGp().getId())) {
////                    log.info("not need {}", compare.getGp().getId());
//                    continue;
//                }
                if (BooleanUtils.isTrue(compare.getResult())) {
                    pass++;
                } else {
                    noPass++;
                }
                compare.checkDataDiff();
                if (compare.diffTotal()) {
                    CompareAssetsVO gp = compare.getGp();
                    CompareAssetsVO ck = compare.getCk();
                    Object proName = proMap.getOrDefault(gp.getId(), gp.getId());
                    Object lastApply = redisTemplate.opsForHash().get(TRANS_APPLY_MAP, gp.getId());
//                    TgApplicationInfo info = new TgApplicationInfo().selectById((Long) lastApply);
                    if (gp.getTotal() < 1) {
                        String applyId = Optional.ofNullable(lastApply).map(Object::toString).orElse("NULL");
                        noDatas.add(new SortString(gp.getId(), Arrays.asList(gp.getId() + "", proName.toString(), "ck=" + ck.getTotal(),
                                applyId,
                                "<a href=\"/tg-easy-fetch/task/applySQL/" + applyId + "\">SQL</a><br>")));
                    } else {
                        String applyId = Optional.ofNullable(lastApply).map(Object::toString).orElse("NULL");
                        diffDatas.add(new SortString(gp.getId(), Arrays.asList(gp.getId() + "", proName.toString(), "gp=" + gp.getTotal(), "ck=" + ck.getTotal(),
                                applyId,
                                "<a href=\"/tg-easy-fetch/task/applySQL/" + applyId + "\">SQL</a><br>")));
                    }
                }
                String msg = compare.getMsg();

                Map<String, Integer> diffMap = compare.diffIndex();
                if (MapUtils.isNotEmpty(diffMap)) {

                    diffMap.forEach((k, v) -> {
                        // 索引 -> 项目id
                        Map<Integer, List<Long>> idxMap = fieldMaps.get(k);
                        if (Objects.isNull(idxMap)) {
                            idxMap = new HashMap<>();
                            fieldMaps.put(k, idxMap);
                        }
                        List<Long> proIds = idxMap.get(v);
                        if (CollectionUtils.isEmpty(proIds)) {
                            idxMap.put(v, new ArrayList<>());
                        }
                        proIds = idxMap.get(v);
                        proIds.add(compare.getGp().getId());
                    });
                    proFieldMap.put(compare.getGp().getId(), diffMap.keySet());
                }

                String[] parts = msg.split("\n");
                for (String part : parts) {
                    if (StringUtils.isBlank(part)) {
                        continue;
                    }
                    String[] tmp = part.split(":");
//                    System.out.println(tmp[0]);
                    String key = StringUtils.trim(tmp[0]);
                    cmap.put(key, Optional.ofNullable(cmap.get(key)).map(v -> v + 1).orElse(1));
                }
            }
        } catch (Exception e) {
            log.error("", e);
            return "ERROR";
        }
        result.append("<h3>核对概览 | <a href=\"/tg-easy-fetch/task/projectCompareList\">SKU项目明细</a> | <a href=\"/tg-easy-fetch/task/brandCompareList\">品牌项目明细</a></h3>");
        result.append(String.format("通过: %s 未通过: %s GP没数据: %s 数据总量差异: %s<br>", pass, noPass, noDatas.size(), diffDatas.size()));

        String errorCount = cmap.entrySet().stream().map(v -> String.format("%6s: %3d个项目出现<br>", v.getKey(), v.getValue())).collect(Collectors.joining("\n"));
        result.append("<br>核对异常的指标 ： 项目数<br> ");
        result.append(errorCount);

        result.append(" <h3> 数据总量对不上的项目 </h3> ");
        Table diffTable = new Table();
        diffTable.addHeader("序号", "ID", "项目名", "GP", "CK", "申请单ID", "链接");
        diffDatas.stream().sorted(Comparator.comparing(SortString::getSort))
                .forEach(v -> diffTable.addRow(v.getCols()));
        result.append(diffTable.renderHtml());

        result.append(" <h3> GP没数据的项目 </h3> ");
        Table noTable = new Table();
        noTable.addHeader("序号", "ID", "项目名", "CK", "申请单ID", "链接");
        noDatas.stream().sorted(Comparator.comparing(SortString::getSort))
                .forEach(v -> noTable.addRow(v.getCols()));
        result.append(noTable.renderHtml());

        result.append("<h2>精度位差异（数据总量不对的不参于统计）： 正数为整数位数，负数为小数位数 方括号内是出现的项目个数</h2><br>");

        // 字段 -> 索引 -> 项目id
        String idxProMsg = fieldMaps.entrySet().stream()
                .map(v -> {
                            String valPair = v.getValue().entrySet().stream()
                                    .map(k -> k.getKey() + "[" + k.getValue().size() + "]  ")
                                    .collect(Collectors.joining(","));

                            Optional<Integer> errCount = v.getValue().values().stream().map(List::size).reduce(Integer::sum);
                            String ids = proFieldMap.entrySet().stream()
                                    .filter(f -> f.getValue().contains(v.getKey()))
                                    .map(Map.Entry::getKey)
                                    .sorted()
                                    .map(Object::toString)
                                    .map(p -> {
                                        long projectId = Long.parseLong(p);
                                        String line = p + " " + proMap.getOrDefault(projectId, p);

                                        StringBuilder builder = new StringBuilder();
                                        for (Map.Entry<Integer, List<Long>> pros : v.getValue().entrySet()) {
                                            if (pros.getValue().contains(projectId)) {
                                                builder.append("「").append(pros.getKey()).append("」,");
                                            }
                                        }
                                        line += builder.toString();
                                        return line;
                                    })
                                    .map(Object::toString)
                                    .collect(Collectors.joining("<br/>"));
                            return String.format("<h3>%6s累计%3d条有误差</h3> 精度位分布: %s<br>项目分布: <br><br> %s<br>",
                                    v.getKey(), errCount.orElse(0), valPair, ids);
//                            return String.format("<h3>%6s", v.getKey()) + "累计" + String.format("%3d", errCount.orElse(0))
//                                    + "条有误差</h3> 精度位分布:  " + valPair + " 项目分布:<br><br> " + ids + " <br>";
                        }
                )
                .collect(Collectors.joining("\n"));

        result.append(idxProMsg);
        String rsp = result.toString();
        if (StringUtils.isNotBlank(plainText)) {
            return rsp.replace("<br>", "");
        }
        return rsp;
    }

    public String allocateToUser() {
        Map applyMap = redisTemplate.opsForHash().entries(TRANS_APPLY_MAP);
        Set<Map.Entry> set = applyMap.entrySet();

        for (Map.Entry entry : set) {
            Long applyId = (Long) entry.getValue();
            if (!Objects.equals(entry.getKey(), 164L)
                    && !Objects.equals(entry.getKey(), 168L)
            ) {
                continue;
            }
            Object applyUser = redisTemplate.opsForHash().get(RedisKeys.Apply.TRANS_USER_MAP, entry.getKey());
            if (Objects.isNull(applyUser)) {
                log.error("no user map {}", entry);
                continue;
            }


            String username = applyUser.toString();
            Map<String, Long> userMap = sysUserService.selectUserByRealNames(Collections.singletonList(username));
            Long userId = userMap.get(username);
            // 回滚申请人
//            userId = transferProperties.getApplicantId();

            if (Objects.isNull(userId)) {
                log.error("user not exist {}", username);
                continue;
            }


            log.info("replace: userId={} applyId={}", userId, applyId);
            TgApplicationInfo info = applicationInfoMapper.selectById(applyId);
            applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>()
                    .lambda()
                    .set(TgApplicationInfo::getApplicantId, userId)
                    .eq(TgApplicationInfo::getId, applyId));

            Long assetsId = info.getAssetsId();
            if (Objects.nonNull(assetsId)) {
                userDataAssetsMapper.update(null, new UpdateWrapper<UserDataAssets>().lambda()
                        .set(UserDataAssets::getApplicantId, userId)
                        .eq(UserDataAssets::getId, assetsId)
                );

//                dataDirMapper.update(null, new UpdateWrapper<DataDir>().lambda()
//                        .eq(DataDir::getIcon, CommonConstants.ICON_DATA_ASSETS)
//                        .eq(DataDir::getNodeId, assetsId)
//                        .set(DataDir::getApplicantId, userId)
//                );
//                nodeMappingMapper.update(null, new UpdateWrapper<TgNodeMapping>().lambda()
//                        .eq(TgNodeMapping::getNodeId, assetsId)
//                        .eq(TgNodeMapping::getIcon, CommonConstants.ICON_DATA_ASSETS)
//                        .set(TgNodeMapping::getApplicantId, userId)
//                );
            }
        }

        return "OK";
    }


    /**
     * 30s
     *
     * @param no excel Id 当等于-1则比较全部
     * @see this#compareForSku
     */
    public AjaxResult<CmhFlowCompareResultVO> compareAssetsForCmhFlow(Integer no, Set<Long> wideIgnore) {
        if (Objects.isNull(no)) {
            return AjaxResult.error("id不正确");
        }
        if (no < 0) {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(RedisKeys.FlowApply.TRANS_APPLY_MAP);
            // 临时可控的使用并行流，生产代码不推荐
            entries.keySet().stream().parallel().forEach(v -> {
                AjaxResult<CmhFlowCompareResultVO> r = this.compareAssetsForCmhFlow(Integer.valueOf(v.toString()), wideIgnore);
                if (!r.isSuccess()) {
                    log.error("Error: {}", r);
                } else {
                    log.info("{}", r.getData());
                }
            });
            return AjaxResult.success("", null);
        }

        if (CollectionUtils.isNotEmpty(wideIgnore) && wideIgnore.contains(no)) {
            return AjaxResult.error("忽略项目");
        }

        Object applyIdO = redisTemplate.opsForHash().get(RedisKeys.FlowApply.TRANS_APPLY_MAP, no);
        if (Objects.isNull(applyIdO)) {
            return AjaxResult.error("该记录" + no + "未创建资产，请检查数据");
        }
        Long applyId = (Long) applyIdO;
        log.warn("cmh flow compare: id={} applyId={}", no, applyId);

        TgApplicationInfo info = applicationInfoMapper.selectById(applyId);
        if (Objects.isNull(info) || Objects.isNull(info.getAssetsId()) || !Objects.equals(info.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.AUDIT_PASS)) {
            return AjaxResult.error("该记录对应申请未审核通过");
        }

        String resultTable = transferProperties.getFlowResultTable();
        CmhFlowCompareVO gp = CmhFlowCompareVO.builder().id(no).build();
        String projectName = info.getProjectName();
        if (projectName.endsWith("_品牌")) {
            projectName = projectName.replace("_品牌", "");
        }
        if (projectName.endsWith("_品牌_test")) {
            projectName = projectName.replace("_品牌_test", "");
        }
        String gpSQL = String.format("SELECT count(*) total, avg(avg_dj) A1, sum(fdxsl) A2,sum(fdxse) A3, avg(sz_phl) A4,"
                        + " avg(jq_phl) A5,avg(dj_cw) A6, sum(fdxsl_cw) A7, sum(fdxse_cw) A8" +
                        " FROM %s WHERE project_name = '%s'",
                resultTable, projectName);
        if (Objects.equals(CommonConstants.SEASON, info.getPeriodType())) {
            gpSQL += " AND period <= '2023Q4'";
        } else if (Objects.equals(CommonConstants.HFYEAR, info.getPeriodType())) {
            gpSQL += " AND period < '202401'";
        } else {
            gpSQL += " AND period <= '202402'";
        }
        this.fillMetricsFromGPResult(gpSQL, gp);

        UserDataAssets assets = userDataAssetsDAO.getById(info.getAssetsId());
        CmhFlowCompareVO ck = CmhFlowCompareVO.builder().id(no).build();

        String ckSQL = String.format("select count(*) total, " +
                "avg(pp_avg_dj) A1, " +
                "sum(pp_fd_xsl) A2, " +
                "sum(pp_fd_xse) A3, " +
                "avg(pp_sz_phl) A4," +
                "avg(pp_jq_phl) A5," +
                "avg(pp_avg_dj_cw) A6," +
                "sum(pp_fd_xsl_cw) A7," +
                "sum(pp_fd_xse_cw) A8"
                + " FROM %s", assets.getAssetTableName());

        // TODO 临时处理规避新数据
        ckSQL += " WHERE period_str <= '202402'";
        this.fillMetricsFromCk(ckSQL, ck, assets.getAssetTableName());
        log.info("qp ck \n{}\n {}", gpSQL, ckSQL);

        // 核对
        CmhFlowCompareResultVO result = CmhFlowCompareResultVO.builder().gp(gp).ck(ck).build();
        result.colsDiff();

//        if (BooleanUtils.isNotTrue(dryRun)) {
//            AcceptRequest accept = new AcceptRequest();
//            accept.setAssetsId(assets.getId());
//            accept.setState(BooleanUtils.isTrue(result.getResult()) ? AcceptanceStateEnum.pass.name() : AcceptanceStateEnum.reject.name());
//            accept.setApplicantId(info.getApplicantId());
//            accept.setRemark("数据迁移验收");
//            accept.setForceRetry(true);
//            AjaxResult<Void> acceptResult = acceptanceRecordService.accept(accept);
//            if (!acceptResult.isSuccess()) {
//                log.warn("acceptResult={}", acceptResult);
//            }
//        }
        redisTemplate.opsForHash().put(RedisKeys.FlowApply.VALIDATE_RESULT_MAP, no, JsonUtils.format(result));
        return AjaxResult.success(result);
    }

    /**
     * 常规 长尾 两种模板的数据比对
     */
    public AjaxResult<CompareResultVO> compareForSku(Long no, Set<Long> wideIgnore,
                                                     String listKey, String resultKey, String gpTable) {
        if (Objects.isNull(no)) {
            return AjaxResult.error("参数为空");
        }
        boolean tail = Objects.equals(listKey, RedisKeys.Apply.TRANS_TAIL_APPLY_MAP);

        // 处理全部数据
        if (no < 0) {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(listKey);
            List<CompareResultVO> voList = entries.keySet().stream().parallel().map(o -> {
                AjaxResult<CompareResultVO> r = this.compareForSku(Long.valueOf(o.toString()),
                        wideIgnore, listKey, resultKey, gpTable);
                if (!r.isSuccess()) {
                    log.error("Error: {}", r);
                    return null;
                }
                return r.getData();
            }).filter(Objects::nonNull).collect(Collectors.toList());
//            log.info("voList={}", voList);
            return AjaxResult.success("", null);
        }

        if (CollectionUtils.isNotEmpty(wideIgnore) && wideIgnore.contains(no)) {
            return AjaxResult.error("忽略项目");
        }
        try {
            Object applyIdO = redisTemplate.opsForHash().get(listKey, no);
            if (Objects.isNull(applyIdO)) {
                return AjaxResult.error("该记录" + no + "未创建资产，请检查数据");
            }
            Long applyId = (Long) applyIdO;
            log.warn("compare: id={} applyId={}", no, applyId);

            TgApplicationInfo info = applicationInfoMapper.selectById(applyId);
            if (Objects.isNull(info.getAssetsId()) || !Objects.equals(info.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.AUDIT_PASS)) {
                return AjaxResult.error("该记录对应申请未审核通过");
            }

            UserDataAssets assets = userDataAssetsDAO.getById(info.getAssetsId());

            CompareAssetsVO gp = CompareAssetsVO.builder().id(no).build();
            String projectName = info.getProjectName();
            if (projectName.endsWith("-长尾")) {
                projectName = projectName.replace("-长尾", "");
            }

            // TODO 硬处理上游数据差异
            String tailSql = tail ? "avg(sz_phl) A3, avg(jq_phl) A4" : "avg(sz_phl*100) A3, avg(jq_phl*100) A4";

            String gpSQL = String.format("SELECT count(*) total, avg(avg_dj) A1, sum(tz_fdxse) A2, %s," +
                    "sum(sample_xse) A5,sum(fd_xsl) A6, sum(ddu) A7 , sum(tv) A8, sum(vpd) A9 " +
                    " FROM %s WHERE project_name = '%s'", tailSql, gpTable, projectName);
            if (Objects.equals(CommonConstants.SEASON, info.getPeriodType())) {
                gpSQL += " AND period <= '2024Q4'";
            } else if (Objects.equals(CommonConstants.HFYEAR, info.getPeriodType())) {
                gpSQL += " AND period < '202501'";
            } else {
                gpSQL += " AND period <= '202411'";
            }
            this.fillMetricsFromGPResult(gpSQL, gp);

            CompareAssetsVO ck = CompareAssetsVO.builder().id(no).build();
            String ckSQL = String.format("select count(*) total, avg(`平均单价`) A1, sum(`放大销售额`) A2, avg(`铺货率`) A3, avg(`加权铺货率`) A4,sum(`样本销售额`)A5\n" +
                            ",sum(`放大销售量`) A6, sum(round(`累计可服用天数`, 0)) A7 , sum(`装量`) A8, sum(`日服用量`) A9 FROM %s",
                    assets.getAssetTableName());

            // TODO 手动设定截至时间
            if (Objects.equals(CommonConstants.HFYEAR, info.getPeriodType())) {
                ckSQL += " where period_new < '2025-01-01' ";
            }

            this.fillMetricsFromCk(ckSQL, ck, assets.getAssetTableName());

            log.info("qp ck \n{}\n {}", gpSQL, ckSQL);

            // 核对
            CompareResultVO result = CompareResultVO.builder().gp(gp).ck(ck).build();
            result.checkDataDiff();

//            if (BooleanUtils.isNotTrue(dryRun)) {
//                AcceptRequest accept = new AcceptRequest();
//                accept.setAssetsId(assets.getId());
//                accept.setState(BooleanUtils.isTrue(result.getResult()) ? AcceptanceStateEnum.pass.name() : AcceptanceStateEnum.reject.name());
//                accept.setApplicantId(info.getApplicantId());
//                accept.setRemark("数据迁移验收");
//                accept.setForceRetry(true);
//                AjaxResult<Void> acceptResult = acceptanceRecordService.accept(accept);
//                if (!acceptResult.isSuccess()) {
//                    log.warn("acceptResult={}", acceptResult);
//                }
//            }
            redisTemplate.opsForHash().put(resultKey, no, JsonUtils.format(result));
            return AjaxResult.success(result);
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    public AjaxResult<CompareResultVO> compareAssetsForSku(Long no, Set<Long> wideIgnore) {
        return this.compareForSku(no, wideIgnore,
                RedisKeys.Apply.TRANS_APPLY_MAP,
                RedisKeys.Apply.VALIDATE_RESULT_MAP,
                transferProperties.getResultTable());
    }

    /**
     * 计算 长尾差异
     */
    public AjaxResult<CompareResultVO> compareAssetsForSkuTail(Long no, Set<Long> wideIgnore) {
        return this.compareForSku(no, wideIgnore,
                RedisKeys.Apply.TRANS_TAIL_APPLY_MAP,
                RedisKeys.Apply.VALIDATE_TAIL_RESULT_MAP,
                transferProperties.getTailResultTable());
    }

    public void fillMetricsFromCk(String sql, CompareModel vo, String table) {
        List<LinkedHashMap<String, Object>> result = ckClusterAdapter.query(table, sql);
        log.info("snap sql={} result={}", sql, result);
        if (CollectionUtils.isEmpty(result)) {
            vo.setTotal(-1L);
            return;
        }

        vo.fillQueryVal(result);
    }

    public void fillMetricsFromGPResult(String sql, CompareModel vo) {
        List<LinkedHashMap<String, Object>> result = ckProviderMapper.selectAllDataFromCk(sql);
        log.info("sql={} result={}", sql, result);
        if (CollectionUtils.isEmpty(result)) {
            vo.setTotal(-1L);
            return;
        }
        vo.fillQueryVal(result);
    }
}
