package com.sinohealth.system.dto.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-24 15:20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTaskPageVO {

    private IPage<AsyncTaskVo> pages;

    private Integer unRead;
}
