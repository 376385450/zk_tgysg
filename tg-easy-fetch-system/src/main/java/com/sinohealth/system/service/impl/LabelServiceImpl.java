package com.sinohealth.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageInfo;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.DelFlag;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.domain.converter.LabelInfoBeanConverter;
import com.sinohealth.system.domain.label.TgAssetLabelRelation;
import com.sinohealth.system.domain.label.TgLabelInfo;
import com.sinohealth.system.dto.label.AddLabelRequest;
import com.sinohealth.system.dto.label.DeleteLabelRequest;
import com.sinohealth.system.dto.label.PageQueryLabelRequest;
import com.sinohealth.system.dto.label.UpdateLabelRequest;
import com.sinohealth.system.mapper.TgAssetInfoMapper;
import com.sinohealth.system.mapper.TgAssetLabelRelationMapper;
import com.sinohealth.system.mapper.TgLabelInfoMapper;
import com.sinohealth.system.service.ILabelService;
import com.sinohealth.system.vo.TgLabelInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/18 16:48
 */
@Slf4j
@Service
@Transactional
public class LabelServiceImpl extends ServiceImpl<TgAssetLabelRelationMapper, TgAssetLabelRelation> implements ILabelService {

    @Resource
    private TgLabelInfoMapper labelInfoMapper;

    @Resource
    private TgAssetLabelRelationMapper assetLabelRelationMapper;

    @Resource
    private TgAssetInfoMapper tgAssetInfoMapper;

    /**
     * 分页查询
     *
     * @param queryLabelRequest
     * @return
     */
    @Override
    public AjaxResult<PageInfo<TgLabelInfoVo>> pageQuery(PageQueryLabelRequest queryLabelRequest) throws InterruptedException {
        // TODO fixme
        Thread.sleep(500l);
        IPage<TgLabelInfoVo> page = labelInfoMapper
                .pageQuery(new Page(queryLabelRequest.getPageNum(), queryLabelRequest.getPageSize()), queryLabelRequest);
        PageInfo<TgLabelInfoVo> pageInfo = PageUtil.convert(page, item -> {
            if (StringUtils.isNotEmpty(item.getAssetNameStr())) {
                item.setAssetNameList(Arrays.asList(item.getAssetNameStr().split("、")));
            }
            return item;
        });
        return AjaxResult.success(pageInfo);
    }

    /**
     * 新增标签
     *
     * @param addLabelRequest
     * @return
     */
    @Override
    public AjaxResult<Object> addLabel(AddLabelRequest addLabelRequest) {
        List<TgLabelInfo> list = labelInfoMapper.selectList(new QueryWrapper<TgLabelInfo>().lambda()
                .eq(TgLabelInfo::getName, addLabelRequest.getName())
                .eq(TgLabelInfo::getDelFlag, DelFlag.NOT_DEL.getCode()));
        if (CollectionUtils.isNotEmpty(list)) {
            return AjaxResult.error("标签名称不能重复");
        }

        TgLabelInfo entity = LabelInfoBeanConverter.toEntity(addLabelRequest);
        int insert = labelInfoMapper.insert(entity);
        log.info("新增标签，结果：{}，标签名：{}", insert, addLabelRequest.getName());
        return AjaxResult.success(entity.getId());
    }

    /**
     * 更新标签
     *
     * @param updateLabelRequest
     * @return
     */
    @Override
    public AjaxResult<Object> updateLabel(UpdateLabelRequest updateLabelRequest) {
        int update = labelInfoMapper.updateById(LabelInfoBeanConverter.toEntity(updateLabelRequest));
        log.info("更新标签，结果：{}，标签ID：{}，新标签名：{}", update, updateLabelRequest.getId(), updateLabelRequest.getName());
        return AjaxResult.success();
    }

    /**
     * 删除标签
     *
     * @param deleteLabelRequest
     * @return
     */
    @Override
    public AjaxResult<Object> deleteLabel(DeleteLabelRequest deleteLabelRequest) {

        // 校验该标签是否有关联资产，若有，则提示不能删除
        List<TgAssetLabelRelation> list = assetLabelRelationMapper.selectList(new QueryWrapper<TgAssetLabelRelation>().lambda()
                .eq(TgAssetLabelRelation::getLabelId, deleteLabelRequest.getId())
                .eq(TgAssetLabelRelation::getDelFlag, DelFlag.NOT_DEL.getCode()));
        if (CollectionUtils.isNotEmpty(list)) {
            return AjaxResult.error("当前标签存在关联资产，请在相关资产移除标签后再删除");
        }

        int delete = labelInfoMapper.updateById(LabelInfoBeanConverter.toEntity(deleteLabelRequest));
        log.info("删除标签，结果：{}，标签ID：{}", delete, deleteLabelRequest.getId());
        return AjaxResult.success();
    }

    /**
     * 更新资产和标签的关联关系
     *
     * @param tgAssetInfo
     */
    @Override
    @Transactional
    public void updateLabelRelation(TgAssetInfo tgAssetInfo) {
        if (tgAssetInfo == null) {
            return;
        }
        List<Integer> labels = tgAssetInfo.getAssetLabels();
        List<String> newLabels = tgAssetInfo.getNewTag();
        Long assetId = tgAssetInfo.getId();

        assetLabelRelationMapper.deleteByAssetId(assetId);
        List<TgAssetLabelRelation> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(labels) || CollectionUtils.isNotEmpty(newLabels)) {

            if (labels != null && !labels.isEmpty()) {
                labels.forEach(item -> {
                    list.add(new TgAssetLabelRelation() {{
                        setAssetId(assetId.intValue());
                        setLabelId(item);
                        setCreateTime(new Date());
                    }});
                });
            }
            if (labels != null && newLabels != null && !newLabels.isEmpty()) {
                // 非数字则要新建标签， 标签资产数据绑定
                newLabels.forEach(item -> {
                    AjaxResult<Object> addLabelResult = addLabel(new AddLabelRequest(item));
                    if (addLabelResult.isSuccess()) {
                        Integer labelId = Integer.parseInt(addLabelResult.getData().toString());
                        labels.add(labelId);
                        list.add(new TgAssetLabelRelation() {{
                            setAssetId(assetId.intValue());
                            setLabelId(labelId);
                            setCreateTime(new Date());
                        }});
                    } else {
                        // 重名创建失败忽略
                    }
                });
                JsonBeanConverter.convert2Json(tgAssetInfo);
                tgAssetInfo.updateById();
            }

            super.saveBatch(list);
        }
    }

    /**
     * 根据资产ID获取标签集合
     *
     * @param assetId
     * @return
     */
    @Override
    public List<String> getLabels(Long assetId) {
        if (assetId == null) {
            return new ArrayList<>(0);
        }
        List<TgAssetLabelRelation> list = assetLabelRelationMapper.selectList(new QueryWrapper<TgAssetLabelRelation>().lambda()
                .eq(TgAssetLabelRelation::getAssetId, assetId)
                .eq(TgAssetLabelRelation::getDelFlag, DelFlag.NOT_DEL.getCode()));
        return list.stream().map(item -> item.getLabelId().toString()).collect(Collectors.toList());
    }

    @Override
    public List<Integer> searchLabelRelate(String labels) {
        if (StringUtils.isEmpty(labels)) {
            return new ArrayList<>();
        }
        final LambdaQueryWrapper<TgLabelInfo> wq = Wrappers.<TgLabelInfo>lambdaQuery()
                .eq(TgLabelInfo::getDelFlag, DelFlag.NOT_DEL.getCode())
                .like(TgLabelInfo::getName, labels);

        final List<Long> tgLabelInfos = labelInfoMapper.selectList(wq)
                .stream().map(TgLabelInfo::getId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(tgLabelInfos)) {
            List<TgAssetLabelRelation> list = assetLabelRelationMapper.selectList(new QueryWrapper<TgAssetLabelRelation>().lambda()
                    .in(TgAssetLabelRelation::getLabelId, tgLabelInfos)
                    .eq(TgAssetLabelRelation::getDelFlag, DelFlag.NOT_DEL.getCode()));
            return list.stream().map(TgAssetLabelRelation::getAssetId).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }

    }

    @Override
    public Map<Long, List<TgLabelInfo>> getFullLabels(List<Long> assetId) {
        if (CollUtil.isEmpty(assetId)) {
            return new HashMap<>();
        }
        List<TgAssetLabelRelation> list = assetLabelRelationMapper.selectList(new QueryWrapper<TgAssetLabelRelation>().lambda()
                .in(TgAssetLabelRelation::getAssetId, assetId)
                .eq(TgAssetLabelRelation::getDelFlag, DelFlag.NOT_DEL.getCode()));

        final Map<Integer, List<TgAssetLabelRelation>> relationMap = list.stream().collect(Collectors.groupingBy(TgAssetLabelRelation::getAssetId));

        final List<Integer> labelIds = list.stream().map(TgAssetLabelRelation::getLabelId).collect(Collectors.toList());

        if (CollUtil.isEmpty(labelIds)) {
            return new HashMap<Long, List<TgLabelInfo>>() {{
                for (Long id : assetId) {
                    put(id, new ArrayList<>());
                }
            }};
        }

        final LambdaQueryWrapper<TgLabelInfo> wq = Wrappers.<TgLabelInfo>lambdaQuery()
                .in(TgLabelInfo::getId, labelIds)
                .eq(TgLabelInfo::getDelFlag, DelFlag.NOT_DEL.getCode());

        final Map<Long, TgLabelInfo> labelIdMap = labelInfoMapper.selectList(wq).stream()
                .collect(Collectors.toMap(TgLabelInfo::getId, v -> v));

        Map<Long, List<TgLabelInfo>> fullLabelMap = new LinkedHashMap<Long, List<TgLabelInfo>>();
        assetId.forEach(id -> {
            final List<TgAssetLabelRelation> tgAssetLabelRelations = relationMap.get(Integer.valueOf(id.toString()));
            List<TgLabelInfo> labels = new ArrayList<>();
            if (CollUtil.isNotEmpty(tgAssetLabelRelations)) {
                for (TgAssetLabelRelation tgAssetLabelRelation : tgAssetLabelRelations) {
                    TgLabelInfo tgLabelInfo = labelIdMap.get(Long.parseLong(tgAssetLabelRelation.getLabelId().toString()));
                    if (Objects.nonNull(tgLabelInfo)) {
                        labels.add(tgLabelInfo);
                    }
                }
            }
            fullLabelMap.put(id, labels);
        });

        return fullLabelMap;
    }
}
