package com.sinohealth.system.domain.value.deliver.strategy;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.util.BooleanUtils;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.dataassets.AssetsExpireEnum;
import com.sinohealth.common.exception.BaseException;
import com.sinohealth.common.exception.ExcelRowLimitException;
import com.sinohealth.system.biz.ck.adapter.CKClusterAdapter;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsSnapshot;
import com.sinohealth.system.biz.dataassets.dto.bo.QueueChannel;
import com.sinohealth.system.biz.dataassets.dto.request.DataPreviewRequest;
import com.sinohealth.system.biz.dataassets.mapper.UserDataAssetsSnapshotMapper;
import com.sinohealth.system.biz.dict.util.ExcelUtil;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgDataDescription;
import com.sinohealth.system.domain.value.deliver.DiskFile;
import com.sinohealth.system.domain.value.deliver.ResourceDeliverStrategy;
import com.sinohealth.system.domain.value.deliver.datasource.ApplicationDataSource;
import com.sinohealth.system.domain.value.deliver.resource.ExcelResource;
import com.sinohealth.system.domain.value.deliver.util.HiddenFieldUtils;
import com.sinohealth.system.dto.ApplicationDataDto;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.mapper.TgCkProviderMapper;
import com.sinohealth.system.mapper.TgCkStreamDao;
import com.sinohealth.system.service.DataDescriptionService;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.util.ApplicationSqlUtil;
import com.sinohealth.system.util.EasyExcelUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 17:23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationExcelDeliverStrategyImpl implements ResourceDeliverStrategy<ApplicationDataSource, ExcelResource> {

    private final IApplicationService applicationService;
    private final TgCkProviderMapper tgCkProviderMapper;
    private final TgApplicationInfoMapper applicationInfoMapper;
    private final UserDataAssetsSnapshotMapper userDataAssetsSnapshotMapper;
    private final DataDescriptionService dataDescriptionService;
    private final ITableInfoService tableInfoService;

    private final TgCkStreamDao tgCkStreamDao;
    private final CKClusterAdapter ckClusterAdapter;
    private final RedisTemplate redisTemplate;

    @Resource
    @Qualifier(ThreadPoolType.ASYNC_TASK)
    private ThreadPoolTaskExecutor pool;


    public static final int MAX_EXPORT = 1048576;

    /**
     * 使用文件流
     *
     * @param dataSource 数据来源
     * @return
     * @throws Exception
     */
    @Override
    public ExcelResource deliver(ApplicationDataSource dataSource) throws Exception {
        final Long assetsId = dataSource.getAssetsId();
        // 客户下载次数-埋点
        FileOutputStream fileOutputStream;
        ExcelWriter excelWriter = null;
        try {
            UserDataAssets dataAssets;
            if (Objects.nonNull(dataSource.getRequestDTO()) && Objects.nonNull(dataSource.getRequestDTO().getVersion())) {
                dataAssets = userDataAssetsSnapshotMapper.selectOne(new QueryWrapper<UserDataAssetsSnapshot>().lambda()
                        .eq(UserDataAssetsSnapshot::getAssetsId, assetsId)
                        .eq(UserDataAssetsSnapshot::getVersion, dataSource.getRequestDTO().getVersion())
                );
            } else {
                dataAssets = UserDataAssets.newInstance().selectById(assetsId);
            }

            String fileName = getFileName(dataAssets);
            // 文件名
            DiskFile diskFile = DiskFile.createTmpFile(fileName + ".xlsx");
            fileOutputStream = new FileOutputStream(diskFile.getFile());
            ExcelWriterBuilder excelWriterBuilder = EasyExcel.write(fileOutputStream);
            excelWriter = EasyExcelUtil.appendConfig(excelWriterBuilder);
            // 表单数据sheet
            writeFormDataSheet(dataSource, dataAssets, excelWriter);
            // 数据说明文档sheet
            writeDescriptionSheet(dataSource, excelWriter);
            return new ExcelResource(diskFile);
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    /**
     * 表单数据sheet
     */
    private void writeFormDataSheet(ApplicationDataSource dataSource, UserDataAssets assets, ExcelWriter excelWriter) {
        final Long assetsId = dataSource.getAssetsId();
        final DataPreviewRequest requestDTO = dataSource.getRequestDTO();
        requestDTO.setPageSize(1);
        AjaxResult<?> queryDataResult = applicationService.queryAssetsDataFromCk(assetsId, requestDTO);
        if (!queryDataResult.isSuccess()) {
            throw new BaseException("导出excel时获取数据失败！" + queryDataResult.getMsg());
        }
        ApplicationDataDto applicationDataFromCk = (ApplicationDataDto) queryDataResult.getData();

        if (AssetsExpireEnum.delete.name().equals(applicationDataFromCk.getExpireType())) {
            // 数据已过期
            throw new BaseException("数据已过期，无法导出");
        }
        if (CollectionUtils.isEmpty(applicationDataFromCk.getHeader()) && CollectionUtils.isEmpty(applicationDataFromCk.getList())) {
            throw new BaseException("数据为空");
        }

//        String sheetName = getFileName(assets);
        String sheetName = "数据源";

        List<Long> tableIds = applicationDataFromCk.getHeader().stream().map(ApplicationDataDto.Header::getTableId)
                .distinct().collect(Collectors.toList());
        List<TableInfo> tableInfos = tableInfoService.getBaseMapper().selectBatchIds(tableIds);
        Map<Long, String> aliasMap = tableInfos.stream().collect(Collectors.toMap(TableInfo::getId,
                TableInfo::getTableAlias, (front, current) -> current));

        TgApplicationInfo apply = applicationInfoMapper.selectOne(new QueryWrapper<TgApplicationInfo>().lambda()
                .select(TgApplicationInfo::getExportProjectName)
                .eq(TgApplicationInfo::getId, assets.getSrcApplicationId()));
        boolean needExportName = BooleanUtils.isTrue(apply.getExportProjectName());
        String[] headers = getHeaders(applicationDataFromCk, aliasMap);

        String[] headerFieldNames = getHeaderFieldNames(applicationDataFromCk);
        List<List<String>> headBlock;
        if (needExportName) {
            headBlock = ExcelUtil.head(headers, ApplicationSqlUtil.PROJECT_NAME_VAL);
        } else {
            headBlock = ExcelUtil.head(headers);
        }

        WriteSheet sheet = EasyExcel.writerSheet(0, sheetName).head(headBlock).build();
        // 追加表别名
        String whereSql = getWhereSql(requestDTO);

//        long dataVolume = getDataVolume(dataSource, assets, whereSql);
        long dataVolume = ckClusterAdapter.mixCount(assets.getAssetTableName(), assets.getAssetsSql(), whereSql);
        // fixme 单sheet大小限制，如果要导出几百万的数据需要分sheet
        if (dataVolume > MAX_EXPORT) {
            throw new ExcelRowLimitException("数据量已超出Excel数据量限制（" + MAX_EXPORT + "），建议缩小数据筛选范围");
        }

        final int QUERY_SIZE = 5000;
        String mode = Optional.ofNullable(redisTemplate.opsForValue().get(RedisKeys.Ftp.STREAM_MODE))
                .map(Object::toString).orElse("block");

        long start = System.currentTimeMillis();
        Consumer<List<LinkedHashMap<String, Object>>> handler = v -> {
            List<List<Object>> objects = HiddenFieldUtils.matchLinesWithProject(headerFieldNames, v, "",
                    needExportName, assets.getProjectName());
            try {
                excelWriter.write(objects, sheet);
            } catch (Exception e) {
                log.error("", e);
                throw new RuntimeException(e);
            }
        };

        if (Objects.equals(mode, "queue")) {
            QueueChannel<LinkedHashMap<String, Object>> channel = QueueChannel.buildExportChannel(20000);
            CountDownLatch latch = new CountDownLatch(1);
            this.startWriter(channel, handler, latch);
            tgCkStreamDao.fetchBatchQueue(assets.getAssetTableName(), assets.getAssetsSql(), whereSql, QUERY_SIZE, channel);
            try {
                latch.await();
            } catch (Exception e) {
                log.error("", e);
            }
        } else {
            tgCkStreamDao.fetchBatch(assets.getAssetTableName(), assets.getAssetsSql(), whereSql, QUERY_SIZE, handler);
        }
        log.info("ExportTime mode:{} {}ms", mode, System.currentTimeMillis() - start);

        // 避免gc
//        int offset = 0;
//        while (offset < dataVolume) {
//            List<Object> lines = getContentLines(dataSource, requestDTO, assets, whereSql, QUERY_SIZE, offset, headerFieldNames);
//            excelWriter.write(lines, sheet);
//            offset += QUERY_SIZE;
//            lines.clear();
//        }
    }

    private void startWriter(QueueChannel<LinkedHashMap<String, Object>> channel,
                             Consumer<List<LinkedHashMap<String, Object>>> handler, CountDownLatch latch) {
        pool.execute(() -> {
            try {
                List<LinkedHashMap<String, Object>> cache = new ArrayList<>(500);
                while (channel.isRunning()) {
                    LinkedHashMap<String, Object> row = channel.poll(20, TimeUnit.MILLISECONDS);
                    if (Objects.isNull(row)) {
                        continue;
                    }
                    cache.add(row);

                    if (cache.size() >= 1000) {
                        handler.accept(cache);
                        cache.clear();
                    }
                }

                channel.drainTo(cache);
                if (!cache.isEmpty()) {
                    handler.accept(cache);
                }
            } catch (Exception e) {
                log.error("", e);
            } finally {
                latch.countDown();
            }
        });


    }

    private void writeDescriptionSheet(ApplicationDataSource dataSource, ExcelWriter excelWriter) throws Exception {
        TgDataDescription description = dataDescriptionService.getByAssetsId(dataSource.getAssetsId());
        if (Objects.isNull(description)) {
            return;
        }

        String sheetName = description.getDocName() + "-数据说明文档";
        String[] headers1 = new String[]{"数据说明", "数据说明"};
        WriteSheet sheet = EasyExcel.writerSheet(1, sheetName).build();
        // 数据说明表格
        WriteTable dataDescriptionTable = EasyExcel.writerTable(0).head(ExcelUtil.head(headers1)).automaticMergeHead(true)
                .useDefaultStyle(false)
                .build();
        List<List<String>> descLines = description.getDataDesc().getList().stream()
                .map(quota -> Lists.newArrayList(quota.getKey(), quota.getValue())).collect(Collectors.toList());
        excelWriter.write(descLines, sheet, dataDescriptionTable);
        // 基础指标表格
        String[] headers2 = new String[]{"基础指标", "基础指标"};
        WriteTable baseTargetTable = EasyExcel.writerTable(1).head(ExcelUtil.head(headers2)).automaticMergeHead(true)
                .useDefaultStyle(false).build();
        List<ArrayList<String>> quotaLines = description.getBaseTarget().getList().stream()
                .map(quota -> Lists.newArrayList(quota.getKey(), quota.getValue())).collect(Collectors.toList());
        excelWriter.write(quotaLines, sheet, baseTargetTable);
    }


    private String getFileName(UserDataAssets assets) {
        return assets.getId() + "_V" + assets.getVersion();
//        return assets.getProjectName();
    }

    private String[] getHeaderFieldNames(ApplicationDataDto applicationDataFromCk) {
        return applicationDataFromCk.getHeader().stream().map(ApplicationDataDto.Header::getFiledName)
                .filter(HiddenFieldUtils.APPLY_PREDICATE)
                .toArray(String[]::new);
    }

    private String getWhereSql(GetDataInfoRequestDTO requestDTO) {
        return applicationService.handleWhereSql(requestDTO);
    }


    private void writeContent(ExcelWriterSheetBuilder sheet, List<Object> lines) {
        log.info("----------------写入Excel信息-------------------");
        sheet.doWrite(lines);
        log.info("已写入行数: " + lines.size());
    }

    private ExcelWriterSheetBuilder getSheet(OutputStream outputStream, String sheetName, String[] headers) throws IOException {
        return EasyExcel.write(outputStream).head(ExcelUtil.head(headers)).sheet(sheetName);
    }

    private List<List<Object>> getContentLines(ApplicationDataSource dataSource, GetDataInfoRequestDTO requestDTO,
                                               UserDataAssets applicationInfo, String whereSql, int QUERY_SIZE,
                                               int offset, String[] fieldNames) {

        requestDTO.setPageSize(QUERY_SIZE);
        requestDTO.setPageNum(offset / QUERY_SIZE + 1);

        List<LinkedHashMap<String, Object>> dataMaps = getDataMaps(dataSource, applicationInfo, whereSql, requestDTO);
        return HiddenFieldUtils.matchLines(fieldNames, dataMaps, null);
    }

    private List<LinkedHashMap<String, Object>> getDataMaps(ApplicationDataSource dataSource, UserDataAssets applicationInfo, String whereSql, GetDataInfoRequestDTO requestDTO) {
        return tgCkProviderMapper.selectApplicationDataFromCk(applicationInfo.getAssetsSql(), whereSql, requestDTO);
    }

    private Long getDataVolume(ApplicationDataSource dataSource, UserDataAssets applicationInfo, String whereSql) {
        return tgCkProviderMapper.selectCountApplicationDataFromCk(applicationInfo.getAssetsSql(), Optional.ofNullable(whereSql).orElse(""));
    }

}
