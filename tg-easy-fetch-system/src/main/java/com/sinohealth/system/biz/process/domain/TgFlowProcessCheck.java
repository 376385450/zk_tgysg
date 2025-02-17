package com.sinohealth.system.biz.process.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.common.core.domain.IdTable;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 每次观测的记录，依据两次观测的差异 推动下游业务执行
 *
 * @author Kuangcp
 * 2024-08-08 15:23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_flow_process_check")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class TgFlowProcessCheck extends Model<TgFlowProcessCheck> implements IdTable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String period;

    /**
     * 多份 , 拼接
     */
    private String qcProdcode;

    /**
     * 注意QC版本数量可以超过品类数（支持重复跑）
     *
     * @see FlowProcessTypeEnum
     */
    private String processType;

    private LocalDateTime createTime;
}
