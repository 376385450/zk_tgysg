package com.sinohealth.system.biz.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.model.LoginUser;
import com.sinohealth.common.enums.dict.DataDictEnum;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.common.utils.poi.easyexcel.DateSetExcelUtil;
import com.sinohealth.system.biz.dict.dao.BizDataDictDefineDAO;
import com.sinohealth.system.biz.dict.dao.BizDataDictValDAO;
import com.sinohealth.system.biz.dict.dao.FieldDictDAO;
import com.sinohealth.system.biz.dict.domain.BizDataDictDefine;
import com.sinohealth.system.biz.dict.domain.BizDataDictVal;
import com.sinohealth.system.biz.dict.domain.FieldDict;
import com.sinohealth.system.biz.dict.dto.BizDataDictPageDTO;
import com.sinohealth.system.biz.dict.dto.BizDataDictValDTO;
import com.sinohealth.system.biz.dict.dto.request.BizDataDictUpsertRequest;
import com.sinohealth.system.biz.dict.dto.request.DataDictPageRequest;
import com.sinohealth.system.biz.dict.dto.request.TryRunSQLRequest;
import com.sinohealth.system.biz.dict.service.BizDataDictService;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.dto.table_manage.DataRangePageDto;
import com.sinohealth.system.dto.table_manage.DataRangeQueryDto;
import com.sinohealth.system.mapper.TgCkProvider;
import com.sinohealth.system.mapper.TgCkProviderMapper;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.impl.ApplicationServiceImpl;
import com.sinohealth.system.util.ApplicationSqlUtil;
import com.sinohealth.system.util.QuerySqlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 15:41
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BizDataDictServiceImpl implements BizDataDictService {

    private final FieldDictDAO fieldDictDAO;
    private final BizDataDictDefineDAO bizDataDictDefineDAO;
    private final BizDataDictValDAO bizDataDictValDAO;
    private final TgCkProviderMapper ckProviderMapper;
    private final QueryDictAdapter queryDictAdapter;
    private final ISysUserService userService;
    private final RedisTemplate redisTemplate;
    private final DictUniqueAdapter dictUniqueAdapter;
    private final AppProperties appProperties;
    private final int maxRow = 2000;

    private final Map<String, Function<BizDataDictUpsertRequest, AjaxResult<Void>>> handleMap = new HashMap<>();

    {
        handleMap.put(DataDictEnum.manual.name(), this::upsertByManual);
        handleMap.put(DataDictEnum.table_import.name(), this::upsertByTableImport);
        handleMap.put(DataDictEnum.table_quote.name(), this::upsertByTableQuote);
        handleMap.put(DataDictEnum.excel_import.name(), this::upsertByManual);
    }

    @Override
    public AjaxResult<IPage<BizDataDictPageDTO>> pageQuery(DataDictPageRequest request) {
        LambdaQueryWrapper<BizDataDictDefine> wrapper = new QueryWrapper<BizDataDictDefine>().lambda()
                .eq(StringUtils.isNotBlank(request.getDictType()), BizDataDictDefine::getDictType, request.getDictType())
                .like(StringUtils.isNotBlank(request.getSearchContent()), BizDataDictDefine::getName, request.getSearchContent())
                .and(org.apache.commons.lang3.StringUtils.isNotBlank(request.getBizType()), v -> v.apply(request.buildBizType()))
                .orderByDesc(BizDataDictDefine::getUpdateTime);

        IPage<BizDataDictDefine> page = bizDataDictDefineDAO.page(request.buildPage(), wrapper);
        Set<Long> userIds = page.getRecords().stream().map(BizDataDictDefine::getUpdater).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> nameMap = userService.selectUserNameMapByIds(userIds);

        return AjaxResult.success(PageUtil.convertMap(page, v -> {
            BizDataDictPageDTO dto = new BizDataDictPageDTO();
            BeanUtils.copyProperties(v, dto);
            dto.setUpdater(nameMap.get(v.getUpdater()));
            return dto;
        }));
    }

    /**
     * @see this#upsertByManual
     */
    @Override
    public AjaxResult<List<BizDataDictValDTO>> readExcel(MultipartFile file) {
        Workbook workBook = null;
        try {
            //解析
            workBook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workBook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();

            Set<String> diffRows = new HashSet<>();
            List<String> rows = new ArrayList<>();

            boolean first = true;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                // 跳过首行
                if (first) {
                    first = false;
                    continue;
                }

                Iterator<Cell> cellIterator = row.cellIterator();
                if (cellIterator.hasNext()) {
                    Object cellValue = DateSetExcelUtil.getCellValue(cellIterator.next());
                    Optional.ofNullable(cellValue).map(Object::toString)
                            .filter(StringUtils::isNotBlank)
                            .ifPresent(v -> {
                                diffRows.add(v);
                                rows.add(v);
                            });
                }

                if (diffRows.size() > maxRow) {
                    return AjaxResult.error("Excel导入仅支持导入" + maxRow + "条数据，请重新导入，谢谢。");
                }
            }

            return AjaxResult.success(rows.stream().distinct().map(BizDataDictValDTO::new).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error("读取Excel失败");
        } finally {
            if (Objects.nonNull(workBook)) {
                try {
                    workBook.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
    }

    public AjaxResult<Void> upsertByTableQuote(BizDataDictUpsertRequest request) {
        AjaxResult<BizDataDictDefine> defineResult = this.saveDictDefine(request);
        if (!defineResult.isSuccess()) {
            return AjaxResult.error(defineResult.getMsg());
        }
        log.info("save sql: define={}", defineResult.getData());
        bizDataDictValDAO.remove(new QueryWrapper<BizDataDictVal>().lambda()
                .eq(BizDataDictVal::getDictId, defineResult.getData().getId()));
        return AjaxResult.succeed();
    }

    /**
     * 预览数据量做限制，导入做校验
     */
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> upsertByTableImport(BizDataDictUpsertRequest request) {
        // 不试运行取数，以页面为准
//        TryRunSQLRequest sqlRequest = TryRunSQLRequest.builder().sql(request.getQuoteSql()).limit(maxRow + 1).build();
//        AjaxResult<List<BizDataDictValDTO>> dataResult = queryDictAdapter.tryRun(sqlRequest);
//        if (!dataResult.isSuccess()) {
//            return AjaxResult.error(dataResult.getMsg());
//        }
        if (request.getValList().size() > maxRow) {
            return AjaxResult.error("维表导入超过数据量限制" + maxRow);
        }

//        request.setValList(dataResult.getData());
        return this.upsertByManual(request);
    }

    public AjaxResult<Void> upsertByManual(BizDataDictUpsertRequest request) {
        if (CollectionUtils.isEmpty(request.getValList())) {
            return AjaxResult.error("字典值未填");
        }
        Set<String> disSet = request.getValList().stream().map(BizDataDictValDTO::getVal).collect(Collectors.toSet());
        if (disSet.size() != request.getValList().size()) {
            return AjaxResult.error("字典值存在重复值");
        }

        AjaxResult<BizDataDictDefine> defineResult = this.saveDictDefine(request);
        if (!defineResult.isSuccess()) {
            return AjaxResult.error(defineResult.getMsg());
        }
        BizDataDictDefine bizDataDictDefine = defineResult.getData();
        AtomicInteger atomicInteger = new AtomicInteger();
        List<BizDataDictVal> values = request.getValList().stream()
                .map(v -> new BizDataDictVal()
                        .setDictId(bizDataDictDefine.getId())
                        .setVal(v.getVal())
                        .setSort(atomicInteger.getAndIncrement())
                )
                .collect(Collectors.toList());
        bizDataDictValDAO.remove(new QueryWrapper<BizDataDictVal>().lambda().eq(BizDataDictVal::getDictId, bizDataDictDefine.getId()));
        bizDataDictValDAO.saveBatch(values, 3000);

        return AjaxResult.succeed();
    }

    /**
     * @see this#upsertByManual
     * @see this#upsertByTableImport
     * @see this#upsertByTableQuote
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> upsert(BizDataDictUpsertRequest request) {
        Function<BizDataDictUpsertRequest, AjaxResult<Void>> func = handleMap.get(request.getDictType());
        if (Objects.isNull(func)) {
            return AjaxResult.error("不支持的字典类型");
        }
        return func.apply(request);
    }

    private AjaxResult<BizDataDictDefine> saveDictDefine(BizDataDictUpsertRequest request) {
        LoginUser user = SecurityUtils.getLoginUser();
        BizDataDictDefine define = new BizDataDictDefine();
        BeanUtils.copyProperties(request, define);
        if (Objects.isNull(request.getId())) {
            define.setCreator(user.getUserId());
        }
        define.setUpdater(user.getUserId());
        List<BizDataDictDefine> repeat = dictUniqueAdapter.checkRepeat(bizDataDictDefineDAO, Collections.singletonList(define));
        if (CollectionUtils.isNotEmpty(repeat)) {
            String msg = repeat.stream().map(v -> String.format("【%s】", v.getName()))
                    .collect(Collectors.joining("、"));
            return AjaxResult.error("字典 " + msg + " 存在重复值，请进行确认，谢谢。");
        }

        if (StringUtils.isNotBlank(request.getQuoteSql())) {
            Pair<String, String> pair = ApplicationSqlUtil.checkSql(request.getQuoteSql());
            define.setQuoteCol(pair.getKey());
            define.setQuoteTable(pair.getValue());
        }

        bizDataDictDefineDAO.saveOrUpdate(define);
        return AjaxResult.success(define);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<List<BizDataDictValDTO>> listDictVal(Long dictId) {
        if (Objects.isNull(dictId)) {
            return AjaxResult.error("参数为空");
        }
        BizDataDictDefine dict = bizDataDictDefineDAO.getById(dictId);
        if (Objects.isNull(dict)) {
            return AjaxResult.error("数据字典不存在");
        }

        if (Objects.equals(dict.getDictType(), DataDictEnum.table_quote.name())) {
            TryRunSQLRequest sqlRequest = TryRunSQLRequest.builder().sql(dict.getQuoteSql()).limit(20).build();
            return queryDictAdapter.tryRun(sqlRequest);
        } else {
            List<BizDataDictVal> valList = bizDataDictValDAO.list(new QueryWrapper<BizDataDictVal>().lambda()
                    .select(BizDataDictVal::getVal).eq(BizDataDictVal::getDictId, dictId).orderByAsc(BizDataDictVal::getSort));
            List<BizDataDictValDTO> result = valList.stream().map(v -> {
                BizDataDictValDTO dto = new BizDataDictValDTO();
                BeanUtils.copyProperties(v, dto);
                return dto;
            }).collect(Collectors.toList());
            return AjaxResult.success(result);
        }
    }

    @Override
    public AjaxResult<Void> deleteById(Long id) {
        List<FieldDict> fieldDicts = fieldDictDAO.queryByDictId(Collections.singleton(id));
        if (CollectionUtils.isNotEmpty(fieldDicts)) {
            return AjaxResult.error("当前内容被引用，请取消引用后再删除");
        }
        if (Objects.equals(appProperties.getCustomerId(), id)) {
            return AjaxResult.error("系统内使用的字典，不允许删除");
        }
        BizDataDictDefine define = bizDataDictDefineDAO.getById(id);
        if (BooleanUtils.isTrue(define.getSystemDict())) {
            return AjaxResult.error("系统内使用的字典，不允许删除");
        }

        bizDataDictDefineDAO.removeById(id);
        bizDataDictValDAO.remove(new QueryWrapper<BizDataDictVal>().lambda().eq(BizDataDictVal::getDictId, id));
        return AjaxResult.succeed();
    }

    @Override
    public List<String> searchDictVal(Long dictId, DataRangeQueryDto rangeQuery) {
        int start = (rangeQuery.getPageNum() - 1) * (rangeQuery.getPageSize());
        int pageSize = rangeQuery.getPageSize();
        LambdaQueryWrapper<BizDataDictVal> queryWrapper = buildCondition(dictId, rangeQuery);
        queryWrapper.last(String.format(TgCkProvider.sqlPagePattern, pageSize, start));
        List<BizDataDictVal> vals = bizDataDictValDAO.list(queryWrapper);
        return vals.stream().map(BizDataDictVal::getVal).collect(Collectors.toList());
    }

    /**
     * @see BizDataDictVal#val
     */
    private static LambdaQueryWrapper<BizDataDictVal> buildCondition(Long dictId, DataRangeQueryDto rangeQuery) {
        String colName = "val";
        StringBuilder sqlBuilder = new StringBuilder("1=1 ");

        String isSelected = rangeQuery.getIsSelected();
        List<String> data = rangeQuery.getData();
        String search = rangeQuery.getSearchContent();

        if (StringUtils.isBlank(isSelected)) {
            // 全部标签
        } else if (isSelected.equals("1")) {
            // 已勾选
            if (data.size() > 0) {
                sqlBuilder.append(" AND ").append(String.format(TgCkProvider.sqlInPattern, colName, " IN ",
                        "'" + StringUtils.join(rangeQuery.getData(), "','") + "'"));
            }
        } else {
            // 未勾选
            if (data.size() > 0) {
                sqlBuilder.append(" AND ").append(String.format(TgCkProvider.sqlInPattern, colName, " NOT IN ",
                        "'" + StringUtils.join(rangeQuery.getData(), "','") + "'"));
            }
        }


        if (StringUtils.isNotBlank(search)) {
            String condition = QuerySqlUtil.buildCondition(colName, rangeQuery);
            if (StringUtils.isNotBlank(condition)) {
                sqlBuilder.append(" AND (").append(condition).append(")");
            }
        }
        String condition = sqlBuilder.toString();
        log.info("column sql={}", condition);
        LambdaQueryWrapper<BizDataDictVal> queryWrapper = new QueryWrapper<BizDataDictVal>().lambda()
                .select(BizDataDictVal::getVal)
                .eq(BizDataDictVal::getDictId, dictId)
                .apply(StringUtils.isNotBlank(condition), condition)
                .orderByAsc(BizDataDictVal::getSort);
        return queryWrapper;
    }

    @Override
    public Integer countSearchDictVal(Long dictId, DataRangeQueryDto rangeQuery) {
        LambdaQueryWrapper<BizDataDictVal> queryWrapper = buildCondition(dictId, rangeQuery);
        return bizDataDictValDAO.count(queryWrapper);
    }

    /**
     * 常规+通用 类型的 申请和模板：字典值 支持级联
     *
     * @see ApplicationServiceImpl#queryDataRange 相同业务
     */
    @Override
    public AjaxResult dataRangeSearch(DataRangeQueryDto dataRange) {
        if (Objects.equals(dataRange.getIsSelected(), "1")
                && org.apache.commons.collections4.CollectionUtils.isEmpty(dataRange.getData())
                && StringUtils.isNoneBlank(dataRange.getSearchContent())) {
            DataRangePageDto dataRangePageDto = new DataRangePageDto(0, 1, 10, Collections.emptyList());
            return AjaxResult.success(dataRangePageDto);
        }

        Integer pageNum = dataRange.getPageNum();
        Integer pageSize = dataRange.getPageSize();

        FieldDict field = fieldDictDAO.getById(dataRange.getFieldDictId());
        if (Objects.isNull(field) || Objects.isNull(field.getDictId())) {
            return AjaxResult.error("字段未关联字典");
        }
        // 剔除自身字段 条件
        if (CollectionUtils.isNotEmpty(dataRange.getFilterItems())) {
            dataRange.getFilterItems().removeIf(v -> Objects.equals(v.getFieldName(), field.getFieldName()));
        }
        BizDataDictDefine dict = bizDataDictDefineDAO.getById(field.getDictId());
        if (Objects.isNull(dict)) {
            return AjaxResult.error("数据字典不存在");
        }

        int total;
        List<String> results;
        // 表引用 支持级联
        if (Objects.equals(dict.getDictType(), DataDictEnum.table_quote.name())) {
            this.preHandleFilter(dataRange, dict);
            if (StringUtils.isBlank(dict.getQuoteCol()) || StringUtils.isBlank(dict.getQuoteTable())) {
                return AjaxResult.error("数据字典配置引用表和字段错误");
            }
            try {
                results = ckProviderMapper.selectDataRangeFromCk(dict.getQuoteCol(), dict.getQuoteTable(), dataRange);
            } catch (Exception sqlException) {
                log.error("", sqlException);
                if (sqlException.getMessage().contains("ILLEGAL_TYPE_OF_ARGUMENT")) {
                    return AjaxResult.error("目前只支持检索字符串类型的字段");
                }
                return AjaxResult.error("查询失败");
            }
            total = ckProviderMapper.countDataRangeFromCk(dict.getQuoteCol(), dict.getQuoteTable(), dataRange);
            total = "".equals(dataRange.getIsSelected()) ? total
                    : "1".equals(dataRange.getIsSelected()) ? dataRange.getData().size()
                    : total - dataRange.getData().size();
            if (Objects.isNull(results)) {
                DataRangePageDto dataRangePageDto = new DataRangePageDto(0, pageNum, pageSize, Collections.emptyList());
                return AjaxResult.success(dataRangePageDto);
            }
            int originSize = results.size();
            results.removeIf(StringUtils::isBlank);
            if (originSize != results.size()) {
                results.add(0, ApplicationSqlUtil.NULL_FLAG);
            }
        } else {
            results = this.searchDictVal(dict.getId(), dataRange);
            total = this.countSearchDictVal(dict.getId(), dataRange);

            int size = CollectionUtils.size(dataRange.getData());

            total = "".equals(dataRange.getIsSelected()) ? total
                    : "1".equals(dataRange.getIsSelected()) ? size
                    : total - size;
        }

        DataRangePageDto dataRangePageDto = new DataRangePageDto(total, pageNum, pageSize, results);
        return AjaxResult.success(dataRangePageDto);
    }

    /**
     * 预处理 级联查询 树结构
     */
    private void preHandleFilter(DataRangeQueryDto dataRange, BizDataDictDefine dict) {
        if (CollectionUtils.isNotEmpty(dataRange.getFilterItems())) {
            Set<Long> fieldIds = dataRange.getFilterItems().stream().map(FilterDTO.FilterItemDTO::getFieldId)
                    .collect(Collectors.toSet());
            List<FieldDict> fieldDicts = fieldDictDAO.listByIds(fieldIds);
            Map<Long, Long> fieldMap = fieldDicts.stream()
                    .filter(v -> Objects.nonNull(v.getDictId()))
                    .collect(Collectors.toMap(FieldDict::getId, FieldDict::getDictId, (front, current) -> current));
            if (MapUtils.isEmpty(fieldMap)) {
                dataRange.setFilterItems(Collections.emptyList());
                return;
            }

            List<BizDataDictDefine> dictList = bizDataDictDefineDAO.listByIds(fieldMap.values());
            Map<Long, BizDataDictDefine> colMap = dictList.stream().collect(Collectors.toMap(BizDataDictDefine::getId,
                    v -> v, (front, current) -> current));
            dataRange.setTargetTable(dict.getQuoteTable());
            // 相同表的不同字段 才构造where语句
            List<FilterDTO.FilterItemDTO> newFilter = new ArrayList<>();
            for (FilterDTO.FilterItemDTO item : dataRange.getFilterItems()) {
                Optional.ofNullable(fieldMap.get(item.getFieldId())).map(colMap::get)
                        .filter(v -> Objects.equals(v.getQuoteTable(), dict.getQuoteTable()))
                        .ifPresent(v -> {
                            newFilter.add(item);
                            item.setFieldName(v.getQuoteCol());
                        });
            }
            dataRange.setFilterItems(newFilter);
        }
    }
}
