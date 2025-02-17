package com.sinohealth.system.biz.table.facade;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.common.enums.TableInfoSnapshotCompareDetailCategory;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.table.dao.TgTableInfoSnapshotCompareDetailDAO;
import com.sinohealth.system.biz.table.domain.TgTableInfoSnapshotCompareDetail;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@AllArgsConstructor
public class TgTableInfoSnapshotCompareDetailFacade {
    private final TgTableInfoSnapshotCompareDetailDAO tgTableInfoSnapshotCompareDetailDAO;

    /**
     * 保存比对详情信息
     *
     * @param taskId         任务id
     * @param category       表类型
     * @param dataSourceType 数据源类型
     * @param tableName      表名称
     * @param attach         附加信息
     */
    public TgTableInfoSnapshotCompareDetail initCompareDetail(Long taskId, TableInfoSnapshotCompareDetailCategory category, DataSourceType dataSourceType, String tableName, String attach) {
        TgTableInfoSnapshotCompareDetail detail = new TgTableInfoSnapshotCompareDetail();
        detail.setCompareId(taskId);
        detail.setProcessTime(0L);
        detail.setCategory(category.getType());
        detail.setDataSource(dataSourceType.name());
        detail.setTableName(tableName);
        detail.setDataCount(0L);
        detail.setAttach(attach);
        detail.setCreateTime(new Date());
        tgTableInfoSnapshotCompareDetailDAO.save(detail);
        return detail;
    }

    /**
     * 更新比对详情信息
     *
     * @param detail      详情信息
     * @param processTime 处理时长
     * @param countNum    统计数
     */
    public void updateCompareDetail(TgTableInfoSnapshotCompareDetail detail, Long processTime, Long countNum, String attach) {
        if (Objects.nonNull(processTime)) {
            detail.setProcessTime(processTime);
        }
        if (Objects.nonNull(countNum)) {
            detail.setDataCount(countNum);
        }
        detail.setUpdateTime(new Date());
        if (StringUtils.isNotBlank(attach)) {
            detail.setAttach(attach);
        }
        tgTableInfoSnapshotCompareDetailDAO.updateById(detail);
    }

    /**
     * 重命名表名
     *
     * @param id        记录编号
     * @param tableName 表名
     */
    public void updateDetailTableName(Long id, String tableName) {
        TgTableInfoSnapshotCompareDetail byId = tgTableInfoSnapshotCompareDetailDAO.getById(id);
        if (Objects.nonNull(byId)) {
            byId.setTableName(tableName);
            tgTableInfoSnapshotCompareDetailDAO.updateById(byId);
        }
    }

    /**
     * 查询对应表得生成记录
     *
     * @param tableName 表名
     * @return 表信息
     */
    public List<TgTableInfoSnapshotCompareDetail> qryCompareTableInfo(String tableName) {
        LambdaQueryWrapper<TgTableInfoSnapshotCompareDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TgTableInfoSnapshotCompareDetail::getTableName, tableName);
        lambdaQueryWrapper.eq(TgTableInfoSnapshotCompareDetail::getDeleted, false);
        return tgTableInfoSnapshotCompareDetailDAO.list(lambdaQueryWrapper);
    }

    /**
     * 根据对比任务编号获取详细信息
     *
     * @param compareId 对比任务编号
     * @return 详细信息
     */
    public List<TgTableInfoSnapshotCompareDetail> queryByCompareId(Long compareId) {
        LambdaQueryWrapper<TgTableInfoSnapshotCompareDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgTableInfoSnapshotCompareDetail::getCompareId, compareId);
        return tgTableInfoSnapshotCompareDetailDAO.list(wrapper);
    }

    /**
     * 删除
     *
     * @param id 主键
     */
    public void remove(Long id) {
        tgTableInfoSnapshotCompareDetailDAO.removeById(id);
    }

    /**
     * 删除
     *
     * @param detail 实体信息
     */
    public void remove(TgTableInfoSnapshotCompareDetail detail) {
        tgTableInfoSnapshotCompareDetailDAO.removeById(detail);
    }
}
