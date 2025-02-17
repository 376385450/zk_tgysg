package com.sinohealth.system.biz.transfer.listener;

import lombok.NoArgsConstructor;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Kuangcp
 * 2024-07-23 10:47
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyCheckCtx {

    List<Integer> excelIds;

    List<Integer> ignoreIds;

    boolean debug;
}
